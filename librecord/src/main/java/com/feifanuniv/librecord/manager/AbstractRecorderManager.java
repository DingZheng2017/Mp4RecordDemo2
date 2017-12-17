package com.feifanuniv.librecord.manager;

import com.feifanuniv.librecord.bean.EncoderParams;

/**
 * Created by dingzheng on 17/12/17.
 */

public abstract class AbstractRecorderManager {
    protected abstract void initRecordProfile(EncoderParams mParams);//初始化

    protected abstract void startRecord();//开始

    protected abstract void inputAudioFrame(byte[] audioBuf, int readBytes, long tsInNanoTime);//输入音频流

    protected abstract void inputVideoFrame(byte[] videoBuf, long tsInNanoTime);//输入视频流

    protected abstract void stopRecord();//停止

}
