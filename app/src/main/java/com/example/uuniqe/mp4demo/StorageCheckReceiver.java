package com.example.uuniqe.mp4demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.StatFs;

/**
 * Created by Dingzheng on 2017/12/28.
 */

public class StorageCheckReceiver extends BroadcastReceiver {
    private static final int PERIOD = 300000; // 5 minutes
    private static final String STORAGE_CHECK_ACTION = "STORAGE_CHECK";


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(STORAGE_CHECK_ACTION)) {
           AlarmUtil.setAlarmTime(context, System.currentTimeMillis()+PERIOD, STORAGE_CHECK_ACTION, 15);
        }
    }
    //计算当前SD卡容量
    public static boolean isAvaiableSpace(int sizeMb) {
        boolean ishasSpace = false;
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            String sdcard = Environment.getExternalStorageDirectory().getPath();
            StatFs statFs = new StatFs(sdcard);
            long blockSize = statFs.getBlockSize();
            long blocks = statFs.getAvailableBlocks();
            long availableSpare = (blocks * blockSize) / (1024 * 1024);
            if (availableSpare > sizeMb) {
                ishasSpace = true;
            }
        }
        return ishasSpace;
    }

}
