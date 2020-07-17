package com.example.alarmclock;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;


public class AlarmReceiver extends BroadcastReceiver {
    String alarmClockTime;
    String alarmClockDescription;
    int alarmClockId;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        alarmClockDescription = intent.getStringExtra(AlarmClock.INTENT_KEY_DESCRIPTION);
        alarmClockTime = intent.getStringExtra(AlarmClock.INTENT_KEY_TIME);
        alarmClockId = intent.getIntExtra(AlarmClock.INTENT_KEY_ID, -1);

        Intent i = new Intent(context, RingtonePlayingService.class);
        context.startService(i);
        showNotification(this.context);

        Intent wakeUpService = new Intent(context, WakeUpService.class);
        wakeUpService.putExtra(AlarmClock.INTENT_KEY_TIME, alarmClockTime);
        wakeUpService.putExtra(AlarmClock.INTENT_KEY_ID, alarmClockId);
        context.startService(wakeUpService);
        Log.d("AAAA", "Alarm started " + alarmClockId);
    }


    public void showNotification(Context context) {

        NotificationManager mNotificationManager;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "notify_001");
        Bundle b = new Bundle();
        b.putString(AlarmClock.INTENT_KEY_DESCRIPTION, alarmClockDescription);
        b.putString(AlarmClock.INTENT_KEY_TIME, alarmClockTime);
        b.putInt(AlarmClock.INTENT_KEY_ID, alarmClockId);

        Intent intent = new Intent(context, AlarmScreenActivity.class);
        intent.putExtras(b);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setVibrate(new long[]{1000, 1000, 1000})
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.clock_icon)
                .setContentTitle("Благотворительный будильник")
                .setContentText(getCurrentTime() + "\nПора вставать! Отключи будильник!")
                .setPriority(Notification.PRIORITY_MAX)
                .setStyle(new NotificationCompat.BigTextStyle());
        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "AlarmClock";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Благотворительный будильник",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        } else {
            context.startActivity(intent);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(0, mBuilder.build());
    }

    private String getCurrentTime() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date currentTime = new Date();
        return dateFormat.format(currentTime);
    }
}