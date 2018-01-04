package com.example.uuniqe.mp4demo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmUtil {

    public static void setAlarmTime(Context context, long timeInMillis,String action, int time) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(action);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,PendingIntent.FLAG_CANCEL_CURRENT);
        int interval = time;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //参数2是开始时间、参数3是允许系统延迟的时间
            am.setWindow(AlarmManager.RTC, timeInMillis, interval, sender);
        } else {
            am.setRepeating(AlarmManager.RTC, timeInMillis, interval, sender);
        }
    }

    public static void canalAlarm(Context context, String action) {
        Intent intent = new Intent(action);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }




}
