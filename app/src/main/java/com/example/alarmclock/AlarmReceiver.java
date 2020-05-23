package com.example.alarmclock;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.alarmclock.MainActivity.h;


public class AlarmReceiver extends BroadcastReceiver {
    private Context context;
    String alarmClockTime;
    String alarmClockDescription;
    int alarmClockId;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;

        alarmClockDescription= intent.getStringExtra(AlarmClock.INTENT_KEY_DESCRIPTION);
        alarmClockTime= intent.getStringExtra(AlarmClock.INTENT_KEY_TIME);
        alarmClockId= intent.getIntExtra(AlarmClock.INTENT_KEY_ID,-1);

        Intent i = new Intent(context, RingtonePlayingService.class);
        context.startService(i);
        showNotification(this.context);

        Intent wakeUpService= new Intent(context,WakeUpService.class);
        wakeUpService.putExtra("timeId", alarmClockTime);
        wakeUpService.putExtra("alarmId", alarmClockId);
        context.startService(wakeUpService);
        Log.d("AAAA", "Alarm started");
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
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT );

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();

        mBuilder.setVibrate(new long[]{ 1000, 1000, 1000 });
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setSmallIcon(R.drawable.clock_icon);
        mBuilder.setContentTitle("Благотворительный будильник");
        mBuilder.setContentText(getCurrentTime()+"\nПора вставать! Отключи будильник!");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);

        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "AlarmClock";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Благотворительный будильник",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);


        }
        else{
            context.startActivity(intent);
        }

        mNotificationManager.notify(0, mBuilder.build());
    }

    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date currentTime = new Date();
        return dateFormat.format(currentTime);
    }
}