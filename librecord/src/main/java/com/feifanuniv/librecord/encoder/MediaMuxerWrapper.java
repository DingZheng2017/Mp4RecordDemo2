package com.feifanuniv.librecord.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;

import com.feifanuniv.librecord.utils.LogUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Mp4封装混合器
 * Created by dingzheng on 2017/12/17.
 */

public class MediaMuxerWrapper {
    private static final String TAG = MediaMuxerWrapper.class.getSimpleName();
    private final String mFilePath;
    private MediaMuxer mMuxer;
    private int mVideoTrackIndex = -1;
    private int mAudioTrackIndex = -1;
    private MediaFormat mVideoFormat;
    private MediaFormat mAudioFormat;

    private long intervalTime;
    private long resumeTime;
    private long pauseTime;
    private long videoLastRecordTime;
    private long audioLastRecordTime;
    private long tempPTS;
    private boolean isPause;

    public void resume() {
        if (isPause) {
            resumeTime = System.nanoTime() / 1000;
            intervalTime += (resumeTime - pauseTime);
            isPause = false;
        }
    }

    public void pause() {
        isPause = true;
        pauseTime = System.nanoTime() / 1000;
    }

    // 文件路径
    public MediaMuxerWrapper(String path) {
        mFilePath = path;
        Object mux = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mux = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            }
        } catch (IOException e) {
            LogUtils.e(TAG, "创建muxer失败", e);
        } finally {
            mMuxer = (MediaMuxer) mux;
        }
    }

    public synchronized void addTrack(MediaFormat format, boolean isVideo) {
        // now that we have the Magic Goodies, start the muxer
        if (mAudioTrackIndex != -1 && mVideoTrackIndex != -1)
            throw new RuntimeException("already add all tracks");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            int track = mMuxer.addTrack(format);
            LogUtils.i(TAG, String.format("addTrack %s result %d", isVideo ? "video" : "audio", track));
            if (isVideo) {
                mVideoFormat = format;
                mVideoTrackIndex = track;
                if (mAudioTrackIndex != -1) {
                    LogUtils.i(TAG, "both audio and video added,and muxer is started");
                    mMuxer.start();
                }
            } else {
                mAudioFormat = format;
                mAudioTrackIndex = track;
                if (mVideoTrackIndex != -1) {
                    mMuxer.start();
                }
            }
        }
    }

    public synchronized void pumpStream(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo, boolean isVideo) {
        if (mAudioTrackIndex == -1 || mVideoTrackIndex == -1) {
            return;
        }
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            // The codec config data was pulled out and fed to the muxer when we got
            // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
        } else if (bufferInfo.size != 0) {
            if (isVideo && mVideoTrackIndex == -1) {
                throw new RuntimeException("muxer hasn't started");
            }

            tempPTS = bufferInfo.presentationTimeUs - intervalTime;
            if (isVideo) {
                if (tempPTS > videoLastRecordTime) {
                    bufferInfo.presentationTimeUs = tempPTS;
                }
                videoLastRecordTime = bufferInfo.presentationTimeUs;
            } else {
                if (tempPTS > audioLastRecordTime) {
                    bufferInfo.presentationTimeUs = tempPTS;
                }
                audioLastRecordTime = bufferInfo.presentationTimeUs;
            }

            outputBuffer.position(bufferInfo.offset);
            outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mMuxer.writeSampleData(isVideo ? mVideoTrackIndex : mAudioTrackIndex, outputBuffer, bufferInfo);
            }
            LogUtils.d(TAG, String.format("sent %s [" + bufferInfo.size + "] with timestamp:[%d] to muxer", isVideo ? "video" : "audio", bufferInfo.presentationTimeUs / 1000));
        }

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            LogUtils.i(TAG, "BUFFER_FLAG_END_OF_STREAM received");
        }
    }

    public synchronized void release() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (mMuxer != null) {
                if (mAudioTrackIndex != -1 && mVideoTrackIndex != -1) {
                    LogUtils.i(TAG, String.format("muxer is started. now it will be stoped."));
                    try {
                        mMuxer.stop();
                        mMuxer.release();
                        pauseTime = 0;
                    } catch (IllegalStateException ex) {
                        LogUtils.e(TAG, String.format("muxer is started. now it will be stoped."), ex);
                    }

                    mAudioTrackIndex = mVideoTrackIndex = -1;
                } else {
                    LogUtils.i(TAG, String.format("muxer is failed to be stoped."));
                }
            }
        }
    }
}
