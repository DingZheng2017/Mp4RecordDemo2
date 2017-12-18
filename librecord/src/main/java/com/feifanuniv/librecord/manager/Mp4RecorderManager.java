package com.feifanuniv.librecord.manager;

import android.util.Log;

import com.feifanuniv.librecord.bean.EncoderParams;
import com.feifanuniv.librecord.encoder.MediaAudioEncoder;
import com.feifanuniv.librecord.encoder.MediaMuxerWrapper;
import com.feifanuniv.librecord.encoder.MediaVideoEncoder;

import java.io.File;

/**
 *  Created by dingzheng on 17/12/17.
 */

public class Mp4RecorderManager extends AbstractRecorderManager{
    public static final boolean DEBUG = true;
    private static final String TAG = "Mp4RecordManager";
    private MediaAudioEncoder mediaAudioEncoder;
    private MediaVideoEncoder mediaVideoEncoder;
    private MediaMuxerWrapper mMuxer;
    private EncoderParams mParams;
    private static Mp4RecorderManager mRecMp4;


    private Mp4RecorderManager() {
    }

    public static Mp4RecorderManager getMp4RecordInstance() {
        if (mRecMp4 == null) {
            mRecMp4 = new Mp4RecorderManager();
        }
        return mRecMp4;
    }


    @Override
    public void initRecordProfile(EncoderParams mParams) {
        this.mParams = mParams;

    }


    @Override
    public void startRecord() {
        if (mParams == null) {
            throw new IllegalStateException("EncoderParams can not be null,need call setEncodeParams method!");
        }

        // 创建音视频编码线程
        mediaVideoEncoder = new MediaVideoEncoder();
        mediaAudioEncoder = new MediaAudioEncoder();
        String fullPath = getSaveFilePath(mParams.getVideoPath(),mParams.getVideoName());


        android.util.Log.d("ysh","tempFile:   "+fullPath);
        mMuxer = new MediaMuxerWrapper(fullPath);
        if (mediaVideoEncoder != null) {
            mediaVideoEncoder.setMuxer(mMuxer, mParams);
        }
        if (mediaAudioEncoder != null) {
            mediaAudioEncoder.setMuxer(mMuxer, mParams);
        }
        // 配置好混合器后启动线程
        mediaVideoEncoder.start();
        mediaAudioEncoder.start();

    }
    private static String getSaveFilePath(String path,String fileName) {
        StringBuilder fullPath = new StringBuilder();
        fullPath.append(path);
        fullPath.append("/");
        fullPath.append( System.currentTimeMillis());
        fullPath.append(fileName);
        fullPath.append(".mp4");

        String string = fullPath.toString();
        File file = new File(string);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        return string;
    }
    @Override
    public void inputAudioFrame(byte[] audioBuf, int readBytes,long tsInNanoTime) {
        if (mediaAudioEncoder != null) {
            mediaAudioEncoder.feedAudioEncoderData(audioBuf,readBytes,tsInNanoTime);
        }

    }

    @Override
    public void inputVideoFrame(byte[] viedoBuf,long tsInNanoTime) {
        if (mediaVideoEncoder != null) {
            mediaVideoEncoder.feedVideoEncoderData(viedoBuf,tsInNanoTime);
        }

    }

    @Override
    public void resumeRecord(boolean isResume) {
        mMuxer.setResumeRecord(isResume);
    }

    @Override
    public void stopRecord() {
        // 停止混合器
        if (mMuxer != null) {
            mMuxer.release();
            mMuxer = null;
            if (Mp4RecorderManager.DEBUG)
                Log.i(TAG, TAG + "---->停止本地录制");
        }
        if (mediaVideoEncoder != null) {
            mediaVideoEncoder.setMuxer(null, null);
        }
        if (mediaAudioEncoder != null) {
            mediaAudioEncoder.setMuxer(null, null);
        }
        // 停止视频编码线程
        if (mediaVideoEncoder != null) {
            mediaVideoEncoder.exit();
            try {
                Thread t2 = mediaVideoEncoder;
                mediaVideoEncoder = null;
                if (t2 != null) {
                    t2.interrupt();
                    t2.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 停止音频编码线程
        if (mediaAudioEncoder != null) {
            mediaAudioEncoder.exit();
            try {
                Thread t1 = mediaAudioEncoder;
                mediaAudioEncoder = null;
                if (t1 != null) {
                    t1.interrupt();
                    t1.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }




}
