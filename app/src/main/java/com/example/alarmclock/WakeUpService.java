package com.example.alarmclock;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.alarmclock.MainActivity.h;

public class WakeUpService extends Service {


    static final  double MINUTE_TIME_TO_AWAKE=0.5;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        final int alarmId = intent.getIntExtra("alarmId",-1);
        final String timeId = intent.getStringExtra("timeId");
        h= new Handler(Looper.getMainLooper());
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String date = sdf.format(addDays( Time.valueOf(timeId+":00"),1));
                ContentValues newValues = new ContentValues();
                newValues.put("alarmId", alarmId);
                newValues.put("alarmTime",date);
                MainActivity.db.insert("penalty", null, newValues);
                Log.d("AAAA","Penalty inserted with values: "+alarmId+" "+date);

                stopSelf();
            }
        }, (long) (MINUTE_TIME_TO_AWAKE*60*1000));


        return START_NOT_STICKY;

    }


    public static Date addDays(Time date, int days) {
        Calendar c = Calendar.getInstance();
        Date calendarDate= new Date();
        calendarDate.setHours(date.getHours());
        calendarDate.setMinutes(date.getMinutes());
        calendarDate.setSeconds(0);

        c.setTime( new Time(calendarDate.getTime()));
        if((date.getHours()<23 || date.getMinutes()+MINUTE_TIME_TO_AWAKE<60)){
            c.add(Calendar.DATE, days);
        }
        return new Date(c.getTimeInMillis());
    }
    @Override
    public void onDestroy()
    {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        h.removeCallbacksAndMessages(null);;
    }
}
