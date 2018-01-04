package com.feifanuniv.librecord.manager;

import com.feifanuniv.librecord.bean.EncoderParams;
import com.feifanuniv.librecord.bean.RecordStatus;
import com.feifanuniv.librecord.encoder.MediaAudioEncoder;
import com.feifanuniv.librecord.encoder.MediaMuxerWrapper;
import com.feifanuniv.librecord.encoder.MediaVideoEncoder;
import com.feifanuniv.librecord.utils.FileUtils;
import com.feifanuniv.librecord.utils.LogUtils;
import com.feifanuniv.libvideoedit.utils.VideoClipUtil;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by dingzheng on 17/12/17.
 */

public class Mp4RecorderManager extends AbstractRecorderManager {
    private static final String TAG = "Mp4RecordManager";
    private MediaAudioEncoder mediaAudioEncoder;
    private MediaVideoEncoder mediaVideoEncoder;
    private MediaMuxerWrapper mMuxer;
    private EncoderParams mParams;
    private static Mp4RecorderManager mRecMp4;
    private boolean sholdAppendMp4;
    private VideoClipUtil videoClipUtil;
    private String inputPath1;
    private String inputPath2;

    private Mp4RecorderManager() {
    }

    private RecordResultHandler mRecordResltHandler;

    public static Mp4RecorderManager getMp4RecordInstance() {
        if (mRecMp4 == null) {
            mRecMp4 = new Mp4RecorderManager();
        }
        return mRecMp4;
    }

    public interface RecordResultHandler {
        void onRecordStatusChange(RecordStatus status);
    }

    public static void setLogEnable(boolean logEnable) {
        LogUtils.setLogEnable(logEnable);
    }

    public void setmRecordResltHandler(RecordResultHandler mRecordResltHandler){
        this.mRecordResltHandler = mRecordResltHandler;
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
        String fullPath =
                FileUtils.generateFileFullPath(mParams.getVideoPath(), mParams.getVideoName(), false);
        android.util.Log.d("yshclip","重复需要重命名fullPath: "+fullPath);
        if (FileUtils.isExit(fullPath)){
            //ToDo:增加合成标记
            android.util.Log.d("yshclip","重复需要重命名");
            sholdAppendMp4 = true;
            videoClipUtil = VideoClipUtil.getInstance();
            videoClipUtil.setOnVideoClipDoneListener(videoClipDoneListener);
            //生成带后缀新文件名
            inputPath1 = fullPath;
            fullPath = FileUtils.generateFileFullPath(mParams.getVideoPath(), mParams.getVideoName(), true);
            inputPath2 = fullPath;
        }
        android.util.Log.d("yshclip","fullPath：  "+fullPath);
        sholdAppendMp4 = true;
        mMuxer = new MediaMuxerWrapper(fullPath);
        if (mediaVideoEncoder != null) {
            mediaVideoEncoder.setMuxer(mMuxer, mParams);
            mediaVideoEncoder.setVideoResultHandler(mRecordResltHandler);
        }
        if (mediaAudioEncoder != null) {
            mediaAudioEncoder.setMuxer(mMuxer, mParams);
        }
        // 配置好混合器后启动线程
        mediaVideoEncoder.start();
        mediaAudioEncoder.start();

    }

    @Override
    public void inputAudioFrame(byte[] audioBuf, int readBytes, long presentationTimeUs) {
        if (mediaAudioEncoder != null) {
            android.util.Log.d("ysh","Audio111 size -->"+audioBuf.length);
            mediaAudioEncoder.feedAudioEncoderData(audioBuf, readBytes, presentationTimeUs);
        }

    }

    @Override
    public void inputVideoFrame(byte[] viedoBuf, long presentationTimeUs) {
        if (mediaVideoEncoder != null) {
            android.util.Log.d("ysh","Video222 size -->"+viedoBuf.length);
            mediaVideoEncoder.feedVideoEncoderData(viedoBuf, presentationTimeUs);
        }

    }

    @Override
    public void resume() {
        if (mMuxer != null) {
            mMuxer.resume();
        }
    }

    @Override
    public void pause() {
        if (mMuxer != null) {
            mMuxer.pause();
        }
    }

    private VideoClipUtil.OnVideoClipDoneListener videoClipDoneListener = new VideoClipUtil.OnVideoClipDoneListener(){
        @Override
        public void onClipDone() {
            android.util.Log.d("yshclip", "clip done");
        }

        @Override
        public void onException(Exception e) {
            android.util.Log.d("yshclip", "exception" + e);
        }
    };

    @Override
    public void stopRecord() {
        // 停止混合器
        if (mMuxer != null) {
            mMuxer.release();
            mMuxer = null;
            LogUtils.i(TAG, TAG + "---->停止本地录制");
        }
        if (sholdAppendMp4){
            //合成MP4
            if (videoClipUtil != null){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            android.util.Log.d("yshclip","开始合成 "+"path1: "+inputPath1+" path2: "+inputPath2);
                            videoClipUtil.appendMp4List(Arrays.asList(inputPath1, inputPath2), inputPath1);
                        } catch (IOException e) {
                            e.printStackTrace();
//                            if (onVideoClipDoneListener != null) {
//                                onVideoClipDoneListener.onException(e);
//                            }
                        }
//                        if (onVideoClipDoneListener != null) {
//                            onVideoClipDoneListener.onClipDone();
//                        }
                    }
                }).start();
            }

            sholdAppendMp4 = false;
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
                LogUtils.e(TAG, "Video Encoder InterruptedException ", e);
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
                LogUtils.e(TAG, "Audio Encoder InterruptedException ", e);
            }
        }
    }
}
