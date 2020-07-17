package com.example.alarmclock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.alarmclock.MainActivity.h;

public class WakeUpService extends Service {


    static final double MINUTE_TIME_TO_AWAKE = 2;

    public static Date addDays(Time date, int days) {
        Calendar c = Calendar.getInstance();
        Date calendarDate = new Date();
        calendarDate.setHours(date.getHours());
        calendarDate.setMinutes(date.getMinutes());
        calendarDate.setSeconds(0);

        c.setTime(new Time(calendarDate.getTime()));
//        if ((date.getHours() < 23 || date.getMinutes() + MINUTE_TIME_TO_AWAKE < 60)) {
//            c.add(Calendar.DATE, days);
//        }
        return new Date(c.getTimeInMillis());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        final int alarmId = intent.getIntExtra(AlarmClock.INTENT_KEY_ID, -1);
        final String timeId = intent.getStringExtra(AlarmClock.INTENT_KEY_TIME);
        h = new Handler(Looper.getMainLooper());
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                String date = sdf.format(addDays(Time.valueOf(timeId + ":00"), 1));
                ContentValues newValues = new ContentValues();
                newValues.put("alarmId", alarmId);
                newValues.put("alarmTime", date);
                MainActivity.db.insert("penalty", null, newValues);
                Log.d("AAAA", "Penalty inserted with values: " + alarmId + " " + date);
                showNotification(WakeUpService.this, date.split(" ")[1]);
                Intent in = new Intent(WakeUpService.this, RingtonePlayingService.class);
                WakeUpService.this.stopService(in);
                stopSelf();
            }
        }, (long) (MINUTE_TIME_TO_AWAKE * 60 * 1000));
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        h.removeCallbacksAndMessages(null);
    }

    public void showNotification(Context context, String timeString) {

        NotificationManager mNotificationManager;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "notify_001");

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        mBuilder.setVibrate(new long[]{1000, 1000, 1000})
                .setContentIntent(contentIntent).setSmallIcon(R.drawable.clock_icon)
                .setContentTitle("Благотворительный будильник")
                .setContentText("Вы проспали будильник на " + timeString)
                .setPriority(Notification.PRIORITY_MAX)
                .setStyle(bigText);
        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "AlarmClock";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Благотворительный будильник",
                    NotificationManager.IMPORTANCE_HIGH);
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        } else {
            context.startActivity(intent);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(0, mBuilder.build());
    }
}
