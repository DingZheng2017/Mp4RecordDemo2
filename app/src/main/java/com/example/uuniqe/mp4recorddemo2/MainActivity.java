package com.example.uuniqe.mp4recorddemo2;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.feifanuniv.librecord.bean.EncoderParams;
import com.feifanuniv.librecord.manager.Mp4RecorderManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
    @BindView(R.id.main_record_btn)
    public Button mBtnRecord;

    @BindView(R.id.pause_btn)
    public Button mBtnPause;

    @BindView(R.id.main_record_surface)
    public SurfaceView mSurfaceView;


    private boolean isRecording;
    private boolean isPause;
    private Mp4RecorderManager mRecMp4;
    private CameraManager mCamManager;
    public static final String ROOT_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath();
    private EncoderParams encoderParams;

    private ExtAudioCapture mExtAudioCapture;
    private static final int AUDIO_BUFFER_SIZE = 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 绑定View
        ButterKnife.bind(this);
        mSurfaceView.getHolder().addCallback(this);

        // 1. 初始化引擎
        mRecMp4 = Mp4RecorderManager.getMp4RecordInstance();
        mCamManager = CameraManager.getCamManagerInstance(this);

        mExtAudioCapture = new ExtAudioCapture();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.main_record_btn, R.id.main_record_surface, R.id.pause_btn})
    public void onViewClick(View v) {
        int vId = v.getId();
        switch (vId) {
            // 录制
            case R.id.main_record_btn:
                if (!isRecording) {
                    // 2. 配置参数
                    encoderParams = new EncoderParams(ROOT_PATH + "/0001", "aa");
                    mRecMp4.initRecordProfile(encoderParams);

                    mExtAudioCapture.startCapture();
                    mExtAudioCapture.setOnAudioFrameCapturedListener(mOnAudioFrameCapturedListener);

                    // 3. 开始录制
                    mRecMp4.startRecord();
                    mBtnRecord.setText("停止录像");
                } else {
                    // 4. 停止录制
                    mRecMp4.stopRecord();
                    mBtnRecord.setText("开始录像");
                }
                isRecording = !isRecording;
                break;
            case R.id.pause_btn:
                if (!isPause){
                    //暂停camera和audio
                    mCamManager.stopPreivew();
                    mExtAudioCapture.setOnAudioFrameCapturedListener(null);

                    mBtnPause.setText("继续");
                } else {
                    //恢复cameraz和audio
                    mCamManager.startPreview();
                    mExtAudioCapture.setOnAudioFrameCapturedListener(mOnAudioFrameCapturedListener);
                    mBtnPause.setText("暂停");
                }
                isPause = !isPause;
                break;
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // 修改默认分辨率
        startCamera(surfaceHolder);

    }

    public void startCamera(SurfaceHolder surfaceHolder) {
        if (mCamManager == null)
            return;
        mCamManager.setSurfaceHolder(surfaceHolder);
        mCamManager.setOnPreviewResult(mPreviewListener);
        mCamManager.createCamera();
        mCamManager.startPreview();
    }


    public void stopCamera() {
        if (mCamManager == null)
            return;
        mCamManager.stopPreivew();
        mCamManager.destoryCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        stopCamera();

    }

    private ExtAudioCapture.OnAudioFrameCapturedListener mOnAudioFrameCapturedListener = new ExtAudioCapture.OnAudioFrameCapturedListener() {
        @Override
        public void onAudioFrameCaptured(byte[] audioData, int size) {
            long timestamp = System.nanoTime() / 1000;
            if (mRecMp4 != null)
                mRecMp4.inputAudioFrame(audioData, size, timestamp);
        }
    };

    // 预览数据处理
    private CameraManager.OnPreviewFrameResult mPreviewListener = new CameraManager.OnPreviewFrameResult() {
        @Override
        public void onPreviewResult(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            int width = 0;
            int height = 0;
            if (parameters != null) {
                width = parameters.getPreviewSize().width;
                height = parameters.getPreviewSize().height;
            }

            // 处理3：yuv转换颜色格式，再编码
            if (mRecMp4 != null) {

                mRecMp4.inputVideoFrame(data, System.nanoTime() / 1000);
            }
            mCamManager.getCameraIntance().addCallbackBuffer(data);
        }
    };

    private static void NV21toI420SemiPlanar(byte[] nv21bytes, byte[] i420bytes,
                                             int width, int height) {
        final int iSize = width * height;
        System.arraycopy(nv21bytes, 0, i420bytes, 0, iSize);

        for (int iIndex = 0; iIndex < iSize / 2; iIndex += 2) {
            i420bytes[iSize + iIndex / 2 + iSize / 4] = nv21bytes[iSize + iIndex]; // U
            i420bytes[iSize + iIndex / 2] = nv21bytes[iSize + iIndex + 1]; // V
        }
    }
}
