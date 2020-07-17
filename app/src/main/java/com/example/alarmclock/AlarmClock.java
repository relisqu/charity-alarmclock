package com.example.alarmclock;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static android.content.Context.ALARM_SERVICE;

public class AlarmClock implements Comparable<AlarmClock>, Serializable {

    static final String INTENT_KEY_DESCRIPTION = "description";
    static final String INTENT_KEY_TIME = "time";
    static final String INTENT_KEY_ID = "id";

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
        this.context = context;
        alarmManager = (AlarmManager) this.context.getSystemService(ALARM_SERVICE);
    }

    public void setId(int id) {
        this.id = id;
        System.out.println(this.id);
    }

    void putExtras() {
        intent = new Intent(this.context, AlarmReceiver.class);
        intent.putExtra(INTENT_KEY_DESCRIPTION, this.description);
        intent.putExtra(INTENT_KEY_TIME, this.stringTime);
        intent.putExtra(INTENT_KEY_ID, this.id);
        System.out.println(this.id + " " + intent.getIntExtra(INTENT_KEY_ID, -1));
        pendingIntent = PendingIntent.getBroadcast(this.context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        if (this.isEnabled) {
            putExtras();
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, toCalendarMillis(), pendingIntent);
        } else {
            try {
                alarmManager.cancel(pendingIntent);
                intent = null;
                pendingIntent = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void setDescription(String description) {
        this.description = description;
        this.description = String.format("%d\u20BD", this.penalty);
    }

    void setRepeat(Repeat repeat) {
        this.repeat = repeat;
    }

    @SuppressLint("DefaultLocale")
    void setPenalty(int penalty) {
        this.penalty = penalty;
        this.description = String.format("%d\u20BD", this.penalty);
    }

    private long toCalendarMillis() {

        String[] timeArray = stringTime.split(":");
        long currentTimeMillis = Calendar.getInstance().getTimeInMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArray[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeArray[1]));
        calendar.set(Calendar.SECOND, 0);
        long alarmTimeMillis = calendar.getTimeInMillis() / 1000 * 1000;

        long timeDifferenceMillis = alarmTimeMillis - currentTimeMillis;
        if (timeDifferenceMillis < 0) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        if (timeDifferenceMillis > TimeUnit.DAYS.toMillis(1)) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        return calendar.getTimeInMillis();
    }

    @Override
    public int compareTo(AlarmClock o) {
        return stringTime.compareTo(o.stringTime);
    }

    void setTime(String time) {
        System.out.println(time);
        this.time = Time.valueOf(time);
    }

    void setStringTime(String time) {
        this.stringTime = time.substring(0, time.length() - 3);
        toCalendarMillis();
    }

    enum DateValue {
        Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
    }

    static class Repeat {
        ArrayList datesList = new ArrayList();
        boolean isRepeated;
        boolean isEveryDay;
    }

}
