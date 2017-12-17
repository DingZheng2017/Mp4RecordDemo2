package com.feifanuniv.librecord.encoder;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import com.feifanuniv.librecord.bean.EncoderParams;
import com.feifanuniv.librecord.manager.Mp4RecorderManager;
import com.jiangdg.yuvosd.YuvUtils;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * YUV视频流进行编码
 * Created by dingzheng on 17/12/17.
 */

public class MediaVideoEncoder extends Thread {
    private static final String TAG = "MediaVideoEncoder";
    private static final String MIME_TYPE = "video/avc";
    // 间隔10s插入一帧关键帧
    private static final int FRAME_INTERVAL = 10;
    // 绑定编码器缓存区超时时间为10s
    private static final int TIMES_OUT = 10000;

    // 硬编码器
    private MediaCodec mVideoEncoder;
    private int mColorFormat;
    private boolean isExit = false;
    private boolean isEncoderStarted = false;

    private boolean isAddKeyFrame = false;
    private WeakReference<EncoderParams> mParamsRef;
    private MediaFormat newFormat;
    private WeakReference<MediaMuxerWrapper> mMuxerRef;

    public synchronized void setMuxer(MediaMuxerWrapper mMuxer, EncoderParams mParams) {
        this.mMuxerRef = new WeakReference<>(mMuxer);
        this.mParamsRef = new WeakReference<>(mParams);
        MediaMuxerWrapper muxer = mMuxerRef.get();

        if (muxer != null && newFormat != null) {
            muxer.addTrack(newFormat, true);
        }
    }

    private int getFrameRate() {
        if (mParamsRef == null)
            return -1;
        EncoderParams mParams = mParamsRef.get();

        return mParams.getFrameRateDegree() == EncoderParams.FrameRate._20fps ? 20 :
                (mParams.getFrameRateDegree() == EncoderParams.FrameRate._25fps ? 25 : 30);
    }

    private int getBitrate() {
        if (mParamsRef == null)
            return -1;
        EncoderParams mParams = mParamsRef.get();
        int mWidth = mParams.getFrameWidth();
        int mHeight = mParams.getFrameHeight();
        int bitRate = (int) (mWidth * mHeight * 20 * 2 * 0.05f);
        if (mWidth >= 1920 || mHeight >= 1920) {
            switch (mParams.getBitRateQuality()) {
                case LOW:
                    bitRate *= 0.75;// 4354Kbps
                    break;
                case MIDDLE:
                    bitRate *= 1.1;// 6386Kbps
                    break;
                case HIGH:
                    bitRate *= 1.5;// 8709Kbps
                    break;
            }
        } else if (mWidth >= 1280 || mHeight >= 1280) {
            switch (mParams.getBitRateQuality()) {
                case LOW:
                    bitRate *= 0.7;// 2580Kbps
                    break;
                case MIDDLE:
                    bitRate *= 1.4;// 3612Kbps
                    break;
                case HIGH:
                    bitRate *= 1.9;// 4902Kbps
                    break;
            }
        } else if (mWidth >= 640 || mHeight >= 640) {
            switch (mParams.getBitRateQuality()) {
                case LOW:
                    bitRate *= 1.4;// 1204Kbps
                    break;
                case MIDDLE:
                    bitRate *= 2.1;// 1806Kbps
                    break;
                case HIGH:
                    bitRate *= 3;// 2580Kbps
                    break;
            }
        }
        return bitRate;
    }

    private void startCodec() {
        isExit = false;
        if (mParamsRef == null)
            return;
        EncoderParams mParams = mParamsRef.get();
        try {
            MediaCodecInfo mCodecInfo = selectSupportCodec(MIME_TYPE);
            if (mCodecInfo == null) {
                if (Mp4RecorderManager.DEBUG)
                    Log.d(TAG, "匹配编码器失败" + MIME_TYPE);
                return;
            }
            mColorFormat = selectSupportColorFormat(mCodecInfo, MIME_TYPE);
            mVideoEncoder = MediaCodec.createByCodecName(mCodecInfo.getName());
        } catch (IOException e) {
            if (Mp4RecorderManager.DEBUG)
                Log.e(TAG, "创建编码器失败" + e.getMessage());
            e.printStackTrace();
        }
        MediaFormat mFormat = MediaFormat.createVideoFormat(MIME_TYPE, mParams.getFrameWidth(), mParams.getFrameHeight());
        mFormat.setInteger(MediaFormat.KEY_BIT_RATE, getBitrate());
        mFormat.setInteger(MediaFormat.KEY_FRAME_RATE, getFrameRate());
        android.util.Log.d("ysh","bitrate" +getBitrate());
        mFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat);         // 颜色格式
        mFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, FRAME_INTERVAL);
        if (mVideoEncoder != null) {
            mVideoEncoder.configure(mFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mVideoEncoder.start();
            isEncoderStarted = true;
            if (Mp4RecorderManager.DEBUG)
                Log.d(TAG, "配置、启动视频编码器");
        }
    }

    private void stopCodec() {
        if (mVideoEncoder != null) {
            mVideoEncoder.stop();
            mVideoEncoder.release();
            mVideoEncoder = null;
            isAddKeyFrame = false;
            isEncoderStarted = false;
            if (Mp4RecorderManager.DEBUG)
                Log.d(TAG, "关闭视频编码器");
        }
    }
    long millisPerframe = 1000 / 20;
    long lastPush = 0;

    @TargetApi(21)
    public void feedVideoEncoderData(byte[] data, long tsInNanoTime) {
        if(!isEncoderStarted || mParamsRef == null) {
            return;
        }
        try {
            if (lastPush == 0) {
                lastPush = System.currentTimeMillis();
            }
            long time = System.currentTimeMillis() - lastPush;
            if (time >= 0) {
                time = millisPerframe - time;
                if (time > 0)
                    Thread.sleep(time / 2);
            }
        EncoderParams mParams = mParamsRef.get();
        int mWidth = mParams.getFrameWidth();
        int mHeight = mParams.getFrameHeight();
        byte[] resultBytes = new byte[mWidth* mHeight * 3 / 2];
        YuvUtils.transferColorFormat(data,mWidth,mHeight,resultBytes,mColorFormat);
        feedCodec(resultBytes, tsInNanoTime);
            if (time > 0)
                Thread.sleep(time / 2);
            lastPush = System.currentTimeMillis();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void feedCodec(byte[] resultBytes, long tsInNanoTime) {
        ByteBuffer[] inputBuffers = mVideoEncoder.getInputBuffers();
        int inputBufferIndex = mVideoEncoder.dequeueInputBuffer(TIMES_OUT);
        if (inputBufferIndex >= 0) {
            // 绑定一个被空的、可写的输入缓存区inputBuffer到客户端
            ByteBuffer inputBuffer = null;
            if (!isLollipop()) {
                inputBuffer = inputBuffers[inputBufferIndex];
            } else {
                inputBuffer = mVideoEncoder.getInputBuffer(inputBufferIndex);
            }
            // 向输入缓存区写入有效原始数据，并提交到编码器中进行编码处理
            inputBuffer.clear();
            inputBuffer.put(resultBytes);
            inputBuffer.clear();
            mVideoEncoder.queueInputBuffer(inputBufferIndex, 0, resultBytes.length, tsInNanoTime, MediaCodec.BUFFER_FLAG_KEY_FRAME);
        }
    }

    @SuppressLint("WrongConstant")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void run() {
        // 初始化编码器
        if (!isEncoderStarted) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startCodec();
        }
        // 如果编码器没有启动或者没有图像数据，线程阻塞等待
        while (!isExit) {
            drainAndWriteData();
        }
        stopCodec();
    }

    /**
     * drain encoded data and write them to muxer
     */
    @SuppressLint("WrongConstant")
    private void drainAndWriteData() {
        ByteBuffer[] outputBuffers = mVideoEncoder.getOutputBuffers();
        // 返回一个输出缓存区句柄，当为-1时表示当前没有可用的输出缓存区
        // mBufferInfo参数包含被编码好的数据，timesOut参数为超时等待的时间
        MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = -1;
        do {
            outputBufferIndex = mVideoEncoder.dequeueOutputBuffer(mBufferInfo, TIMES_OUT);
            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (Mp4RecorderManager.DEBUG)
                    Log.i(TAG, "获得编码器输出缓存区超时");
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // 如果API小于21，APP需要重新绑定编码器的输入缓存区；
                // 如果API大于21，则无需处理INFO_OUTPUT_BUFFERS_CHANGED
                if (!isLollipop()) {
                    outputBuffers = mVideoEncoder.getOutputBuffers();
                }
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // 编码器输出缓存区格式改变，通常在存储数据之前且只会改变一次
                // 这里设置混合器视频轨道，如果音频已经添加则启动混合器（保证音视频同步）
                synchronized (MediaVideoEncoder.this) {
                    newFormat = mVideoEncoder.getOutputFormat();
                    if (mMuxerRef != null) {
                        MediaMuxerWrapper muxer = mMuxerRef.get();
                        if (muxer != null) {
                            muxer.addTrack(newFormat, true);
                        }
                    }
                }
                if (Mp4RecorderManager.DEBUG)
                    Log.i(TAG, "编码器输出缓存区格式改变，添加视频轨道到混合器");
            } else {
                // 获取一个只读的输出缓存区inputBuffer ，它包含被编码好的数据
                ByteBuffer outputBuffer = null;
                if (!isLollipop()) {
                    outputBuffer = outputBuffers[outputBufferIndex];
                } else {
                    outputBuffer = mVideoEncoder.getOutputBuffer(outputBufferIndex);
                }
                // 如果API<=19，需要根据BufferInfo的offset偏移量调整ByteBuffer的位置
                // 并且限定将要读取缓存区数据的长度，否则输出数据会混乱
                if (isKITKAT()) {
                    outputBuffer.position(mBufferInfo.offset);
                    outputBuffer.limit(mBufferInfo.offset + mBufferInfo.size);
                }
                // 根据NALU类型判断帧类型
                int type = outputBuffer.get(4) & 0x1F;
                if (Mp4RecorderManager.DEBUG)
                    Log.d(TAG, "------还有数据---->" + type);
                if (type == 7 || type == 8) {
                    if (Mp4RecorderManager.DEBUG)
                        Log.e(TAG, "------PPS、SPS帧(非图像数据)，忽略-------");
                    mBufferInfo.size = 0;
                } else if (type == 5) {
                    // 录像时，第1秒画面会静止，这是由于音视轨没有完全被添加
                    // Muxer没有启动
                    // 添加视频流到混合器
                    if (mMuxerRef != null) {
                        MediaMuxerWrapper muxer = mMuxerRef.get();
                        if (muxer != null) {
                            Log.i(TAG, "------编码混合  视频关键帧数据-----");
                            muxer.pumpStream(outputBuffer, mBufferInfo, true);
                        }
                        isAddKeyFrame = true;
                    }
                } else {
                    if (isAddKeyFrame) {
                        // 添加视频流到混合器
                        if (isAddKeyFrame && mMuxerRef != null) {
                            MediaMuxerWrapper muxer = mMuxerRef.get();
                            if (muxer != null) {
                                Log.i(TAG, "------编码混合  视频普通帧数据-----" + mBufferInfo.size);
                                muxer.pumpStream(outputBuffer, mBufferInfo, true);
                            }
                        }
                    }
                }
                // 处理结束，释放输出缓存区资源
                mVideoEncoder.releaseOutputBuffer(outputBufferIndex, false);
            }
        } while (outputBufferIndex >= 0);
    }

    public void exit() {
        isExit = true;
    }

    /**
     * 遍历所有编解码器，返回第一个与指定MIME类型匹配的编码器
     * 判断是否有支持指定mime类型的编码器
     */
    private MediaCodecInfo selectSupportCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            // 判断是否为编码器，否则直接进入下一次循环
            if (!codecInfo.isEncoder()) {
                continue;
            }
            // 如果是编码器，判断是否支持Mime类型
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    /**
     * 根据mime类型匹配编码器支持的颜色格式
     */
    private int selectSupportColorFormat(MediaCodecInfo mCodecInfo, String mimeType) {
        MediaCodecInfo.CodecCapabilities capabilities = mCodecInfo.getCapabilitiesForType(mimeType);
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int colorFormat = capabilities.colorFormats[i];
            if (isCodecRecognizedFormat(colorFormat)) {
                return colorFormat;
            }
        }
        return 0;
    }

    private boolean isCodecRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
                //video/avc编码器支持COLOR_FormatYUV420SemiPlanar格式
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                return true;
            default:
                return false;
        }
    }

    private boolean isLollipop() {
        // API>=21
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    private boolean isKITKAT() {
        // API<=19
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT;
    }
}
