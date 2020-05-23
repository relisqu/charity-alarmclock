package com.example.alarmclock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AlarmClockReceiverWorker extends Worker {
    private Context context;
    String alarmClockTime;
    String alarmClockDescription;
    int alarmClockId;
    WorkerParameters params;
    public AlarmClockReceiverWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context=context;
        this.params=params;
    }

    @Override
    public Result doWork() {

        // Do the work here--in this case, upload the images.

        this.context=context;

        alarmClockDescription= params.getInputData().getString(AlarmClock.INTENT_KEY_DESCRIPTION);
        alarmClockTime= params.getInputData().getString(AlarmClock.INTENT_KEY_TIME);
        alarmClockId= params.getInputData().getInt(AlarmClock.INTENT_KEY_ID,-1);

        Intent i = new Intent(context, RingtonePlayingService.class);
        context.startService(i);
        showNotification(this.context);

        Intent wakeUpService= new Intent(context,WakeUpService.class);
        wakeUpService.putExtra("timeId", alarmClockTime);
        wakeUpService.putExtra("alarmId", alarmClockId);
        context.startService(wakeUpService);
        Log.d("AAAA", "Alarm started");
        // Indicate whether the task finished successfully with the Result
        return Result.success();
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
