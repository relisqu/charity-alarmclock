package com.example.alarmclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static android.content.Context.ALARM_SERVICE;

public class AlarmClock implements Comparable<AlarmClock>, Serializable
{

    public static final String INTENT_KEY_DESCRIPTION = "description";
    public static final String INTENT_KEY_TIME = "time";
    public static final String INTENT_KEY_ID = "id";
    
    
    int id;
    boolean isEnabled;
    Time time;
    Context context;
    Repeat repeat;
    String description;
    String stringTime;
    int penalty;
    PendingIntent pendingIntent;
    AlarmManager alarmManager;
    Intent intent;
    AlarmClock(Context context) {
        this.context=context;
        alarmManager = (AlarmManager) this.context.getSystemService(ALARM_SERVICE);



    }

    public void setId(int id) {
        this.id = id;
    }
    public void putExtras(){

        intent = new Intent(this.context, AlarmReceiver.class);
        intent.putExtra(INTENT_KEY_DESCRIPTION, this.description);
        intent.putExtra(INTENT_KEY_TIME,  this.stringTime);
        intent.putExtra(INTENT_KEY_ID, this.id);
        pendingIntent = PendingIntent.getBroadcast(this.context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        putExtras();
        if (this.isEnabled) {
            Toast.makeText(context, "Alarm on "+stringTime, Toast.LENGTH_LONG).show();
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, toCalendarMillis(),
//                    AlarmManager.INTERVAL_DAY, pendingIntent);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,toCalendarMillis(),pendingIntent);
        } else {
            try {
                alarmManager.cancel(pendingIntent);
                intent=null;
                pendingIntent=null;
                Toast.makeText(context, "ALARM OFF "+stringTime, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setDescription(String description) {
        this.description = description;
        this.description = String.format("%d\u20BD", this.penalty);
    }

    public void setRepeat(Repeat repeat) {
        this.repeat = repeat;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
        this.description = String.format("%d\u20BD", this.penalty);
    }
    public long toCalendarMillis() {

        String[] timeArray = stringTime.split(":");
        long currentTimeMillis = Calendar.getInstance().getTimeInMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArray[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeArray[1]));
        calendar.set(Calendar.SECOND,0);
        long alarmTimeMillis = calendar.getTimeInMillis()/1000*1000;

        long timeDifferenceMillis = alarmTimeMillis - currentTimeMillis;
        if (timeDifferenceMillis < 0) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        if (timeDifferenceMillis > TimeUnit.DAYS.toMillis(1)) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        System.out.println(calendar.getTime().getDate());
        return calendar.getTimeInMillis();
    }
    @Override
    public int compareTo(AlarmClock o) {
        return stringTime.compareTo(o.stringTime);
    }

    public void setTime(String time) {
        System.out.println(time);
        this.time = Time.valueOf(time);
    }

    public void setStringTime(String time) {
        this.stringTime = time.substring(0, time.length() - 3);
        toCalendarMillis();
        System.out.println(time.substring(0, time.length() - 3));
    }

    enum DateValue {
        Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
    }

    public static class Repeat {
        List<DateValue> datesList = new ArrayList();
        boolean isRepeated;
        boolean isEveryDay;
    }

}
