package com.feifanuniv.librecord.bean;

import android.media.AudioFormat;
import android.media.MediaRecorder;

import com.feifanuniv.librecord.encoder.MediaVideoEncoder;

/**
 * 音、视频编码参数
 * Created by dingzheng on 17/12/17.
 */

public class EncoderParams {
    private String videoPath;
    private String videoName;
    private int frameWidth;     // 图像宽度
    private int frameHeight;    // 图像高度
    private Quality bitRateQuality;   // 视频编码码率,0(低),1(中),2(高)
    private FrameRate frameRateDegree; // 视频编码帧率,0(低),1(中),2(高)

    private int audioBitrate;   // 音频编码比特率
    private int audioChannelCount; // 通道数据
    private int audioSampleRate;   // 采样率

    private int audioChannelConfig; // 单声道或立体声
    private int audioFormat;    // 采样精度
    private int audioSouce;     // 音频来源

    /*视频部分*/
    /**
     * 默认图像宽度 1280
     */
    public static final int DEFAULT_PREVIEW_WIDTH = 1280;

    /**
     * 默认图像高度 720
     */
    public static final int DEFAULT_PREVIEW_HEIGHT = 720;


    // 码率等级
    public enum Quality {
        LOW, MIDDLE, HIGH
    }

    // 帧率
    public enum FrameRate {
        _20fps, _25fps, _30fps
    }


    /*音频部分*/
    /**
     * 默认比特率
     */
    public static final int DEFAULT_BIT_RATE = 16000;
    /**
     * 默认采样率
     */
    public static final int DEFAULT_SAMPLE_RATE = 8000;

    /**
     * 通道数为1
     */
    public static final int CHANNEL_COUNT_MONO = 1;
    /**
     * 通道数为2
     */
    public static final int CHANNEL_COUNT_STEREO = 2;
    /**
     * 单声道
     */
    public static final int CHANNEL_IN_MONO = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 立体声
     */
    public static final int CHANNEL_IN_STEREO = AudioFormat.CHANNEL_IN_STEREO;
    /**
     * 16位采样精度
     */
    public static final int ENCODING_PCM_16BIT = AudioFormat.ENCODING_PCM_16BIT;
    /**
     * 8位采样精度
     */
    public static final int ENCODING_PCM_8BIT = AudioFormat.ENCODING_PCM_8BIT;
    /**
     * 音频源为MIC
     */
    public static final int SOURCE_MIC = MediaRecorder.AudioSource.MIC;
    /**
     * 音频源为Default
     */
    public static final int SOURCE_DEFAULT = MediaRecorder.AudioSource.DEFAULT;
    /**
     * 音频源为蓝牙
     */
    public static final int SOURCE_COMMUNICATION = MediaRecorder.AudioSource.VOICE_COMMUNICATION;




    public EncoderParams(String videoPath, String videoName) {

        /*视频设置*/
        this.videoPath = videoPath;
        this.videoName = videoName;
        //720p  1280*720
        this.frameWidth = DEFAULT_PREVIEW_WIDTH;
        this.frameHeight = DEFAULT_PREVIEW_HEIGHT;
        this.bitRateQuality = Quality.LOW;
        this.frameRateDegree = FrameRate._20fps;

        /*音频设置*/
        // 音频比特率
        this.audioBitrate = DEFAULT_BIT_RATE;
        // 音频采样率
        this.audioSampleRate = DEFAULT_SAMPLE_RATE;
        // 单声道
        this.audioChannelConfig = CHANNEL_IN_MONO;
        // 单声道通道数量
        this.audioChannelCount = CHANNEL_COUNT_MONO;
        // 采样精度为16位
        this.audioFormat = ENCODING_PCM_16BIT;
        //音频源为MIC
        this.audioSouce = SOURCE_MIC;

    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public int getAudioChannelConfig() {
        return audioChannelConfig;
    }

    public void setAudioChannelConfig(int audioChannelConfig) {
        this.audioChannelConfig = audioChannelConfig;
    }


    public int getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(int audioFormat) {
        this.audioFormat = audioFormat;
    }

    public int getAudioSouce() {
        return audioSouce;
    }

    public void setAudioSouce(int audioSouce) {
        this.audioSouce = audioSouce;
    }

    public int getAudioChannelCount() {
        return audioChannelCount;
    }

    public void setAudioChannelCount(int audioChannelCount) {
        this.audioChannelCount = audioChannelCount;
    }

    public int getAudioBitrate() {
        return audioBitrate;
    }

    public void setAudioBitrate(int audioBitrate) {
        this.audioBitrate = audioBitrate;
    }

    public int getAudioSampleRate() {
        return audioSampleRate;
    }

    public void setAudioSampleRate(int audioSampleRate) {
        this.audioSampleRate = audioSampleRate;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }

    public FrameRate getFrameRateDegree() {
        return frameRateDegree;
    }

    public void setFrameRateDegree(FrameRate frameRateDegree) {
        this.frameRateDegree = frameRateDegree;
    }

    public Quality getBitRateQuality() {
        return bitRateQuality;
    }

    public void setBitRateQuality(Quality bitRateQuality) {
        this.bitRateQuality = bitRateQuality;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }
}
