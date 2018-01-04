package com.feifanuniv.librecord.bean;

/**
 * Created by SEELE on 2017/12/25.
 */

public enum RecordStatus {
    START, //开始录制
    STOP, //停止
    PAUSE, //暂停
    ERROR, //其他出错
    ERROR_STORAGE,//存储空间不足
    ERROR_VIDEO_ENCODER,//视频编码器错误
    ERROR_VIDEO,//视频源错误
    ERROR_AUDIO,//音源错误
    SUCCESS //成功
}
