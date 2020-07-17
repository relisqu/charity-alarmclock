package com.example.alarmclock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TimePicker;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {
    public static AlarmClockAdapter adapter;
    public static SQLiteDatabase db;
    public static List<AlarmClock> list = new ArrayList<>();
    public static Handler h = new Handler(Looper.getMainLooper());
    final Random random = new Random();
    ListView alarmList;
    ViewGroup root;

    public static void applyDim(@NonNull ViewGroup parent, float dimAmount) {

        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        dim.setAlpha((int) (255 * dimAmount));
        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.add(dim);
    }

    public static void clearDim(@NonNull ViewGroup parent) {
        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = getBaseContext().openOrCreateDatabase("alarmClock.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS penalty ( alarmId INTEGER, alarmTime TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS alarms ( alarmId INTEGER, description TEXT, penalty INTEGER," +
                " time Text, isEnabled NUMERIC, isRepeatable NUMERIC," +
                " isRepeatedOnSunday NUMERIC, isRepeatedOnMonday NUMERIC, isRepeatedOnTuesday NUMERIC, isRepeatedOnWednesday NUMERIC, " +
                "isRepeatedOnThursday NUMERIC, isRepeatedOnFriday NUMERIC, isRepeatedOnSaturday NUMERIC )");
        Cursor cursor = db.rawQuery("select * from penalty", null);
        setContentView(R.layout.activity_main);
        Button penaltyButton = findViewById(R.id.penaltyButton);

        root = (ViewGroup) getWindow().getDecorView().getRootView();

        penaltyButton.setVisibility(View.INVISIBLE);
        getInitialData();
        adapter = new AlarmClockAdapter(this, R.layout.alarm_clock_adapter, list);
        sortArray();

        alarmList = findViewById(R.id.alarmClockList);
        alarmList.setAdapter(adapter);
        alarmList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                onSettingsPopupWindowClick(view, list.get((int) id));
                return false;
            }
        });
        Button button = findViewById(R.id.floating_action_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonShowPopupWindowClick(v);
            }
        });
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                System.out.println(cursor.getInt(0) + " penalty id");
                String d = cursor.getString(1);
                try {
                    Date neededDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(d);
                    if (neededDate.before(new Date())) {
                        penaltyButton.setVisibility(View.VISIBLE);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        penaltyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, PenaltyActivity.class));
                MainActivity.this.finish();
            }
        });
    }

    public void finish() {
        super.finish();
    }

    public void setTimePickerSettings(TimePicker timepicker, int hours, int minutes) {
        timepicker.setIs24HourView(true);
        timepicker.setHour(hours);
        timepicker.setMinute(minutes);
    }

    View.OnClickListener cancelPopupListener(final PopupWindow window) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
            }
        };
    }

    PopupWindow.OnDismissListener removeDimListener() {
        return new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                clearDim(root);
            }
        };
    }

    public void onButtonShowPopupWindowClick(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.create_alarm_clock_activity, null);
        final TimePicker timepicker = popupView.findViewById(R.id.timePicker);
        final EditText penaltyField = popupView.findViewById(R.id.penaltyField);
        final PopupWindow popupWindow = createPopupWindow(popupView);

        Button createButton = popupView.findViewById(R.id.confirmButton);
        Button cancelButton = popupView.findViewById(R.id.cancelButton);
        timepicker.setIs24HourView(true);
        cancelButton.setOnClickListener(cancelPopupListener(popupWindow));
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!penaltyField.getText().toString().equals("") && Integer.parseInt(penaltyField.getText().toString()) > 0) {
                    int penalty = Integer.parseInt(penaltyField.getText().toString());
                    String time = timepicker.getHour() >= 10 ? timepicker.getHour() + "" : "0" + timepicker.getHour();
                    time += ":" + (timepicker.getMinute() >= 10 ? timepicker.getMinute() + "" : "0" + timepicker.getMinute()) + ":00";

                    AlarmClock clock = new AlarmClock(MainActivity.this);
                    clock.setTime(time);
                    clock.setPenalty(penalty);
                    clock.setStringTime(time);
                    clock.setRepeat(new AlarmClock.Repeat());
                    clock.setDescription("");
                    clock.repeat.isRepeated = true;
                    clock.setId(generateId());
                    clock.setEnabled(true);
                    setOnSaving(clock);
                    popupWindow.dismiss();
                }
            }
        });

    }

    public PopupWindow createPopupWindow(View view) {
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        PopupWindow window = new PopupWindow(view, width, height, true);
        window.showAtLocation(view, Gravity.CENTER_HORIZONTAL, 0, 0);
        applyDim(root, 0.5f);
        window.setOnDismissListener(removeDimListener());
        return window;
    }

    @SuppressLint("SetTextI18n")
    public void onSettingsPopupWindowClick(View view, final AlarmClock clock) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.alarm_settings, null);

        final TimePicker timepicker = popupView.findViewById(R.id.timeSettingsPicker);
        final Button cancelButton = popupView.findViewById(R.id.cancelSettingsButton);
        final EditText penaltyField = popupView.findViewById(R.id.penaltySettingsField);
        Button deleteButton = popupView.findViewById(R.id.deleteSettingsButton);
        Button createButton = popupView.findViewById(R.id.confirmSettingsButton);

        final PopupWindow popupWindow = createPopupWindow(popupView);
        setTimePickerSettings(timepicker, clock.time.getHours(), clock.time.getMinutes());
        penaltyField.setText(Integer.toString(clock.penalty));

        cancelButton.setOnClickListener(cancelPopupListener(popupWindow));
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.db.execSQL("DELETE FROM alarms WHERE alarmId=" + clock.id + ";");
                clock.setEnabled(false);
                list.remove(clock);
                adapter.clear();
                adapter.addAll(list);
                adapter.notifyDataSetChanged();
                popupWindow.dismiss();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!penaltyField.getText().toString().equals("")
                        && Integer.parseInt(penaltyField.getText().toString()) > 0) {
                    int penalty = Integer.parseInt(penaltyField.getText().toString());
                    String time = timepicker.getHour() >= 10 ? timepicker.getHour() + "" : "0" + timepicker.getHour();
                    time += ":" + (timepicker.getMinute() >= 10 ? timepicker.getMinute() + "" : "0" + timepicker.getMinute()) + ":00";

                    clock.setTime(time);
                    clock.setPenalty(penalty);
                    clock.setStringTime(time);
                    clock.setRepeat(new AlarmClock.Repeat());
                    clock.setDescription("");

                    ContentValues cv = new ContentValues();
                    cv.put("description", clock.description);
                    cv.put("penalty", clock.penalty);
                    cv.put("time", clock.time.toString());
                    cv.put("isEnabled", clock.isEnabled ? "1" : "0");

                    MainActivity.db.update("alarms", cv, "alarmId = ?", new String[]{clock.id + ""});
                    sortArray();
                    clock.setEnabled(false);
                    adapter.notifyDataSetChanged();
                    popupWindow.dismiss();
                }
            }
        });

    }

    public void setOnSaving(AlarmClock alarmClock) {

        MainActivity.db.execSQL("INSERT INTO alarms VALUES( " + alarmClock.id + ",'" + alarmClock.description + "'," +
                "" + alarmClock.penalty + ",'" + alarmClock.time.toString() + "'," + (alarmClock.isEnabled ? "1" : "0") + "," +
                "" + (alarmClock.repeat.isRepeated ? "1" : "0") + ",'','','','','','','')");

        list.add(alarmClock);
        sortArray();
        adapter.clear();
        adapter.addAll(list);
        adapter.notifyDataSetChanged();
    }

    public void sortArray() {

        AlarmClock[] array = list.toArray(new AlarmClock[0]);
        Arrays.sort(array);
        list = Arrays.asList(array);
        list = new ArrayList<>(list);
    }

    private void getInitialData() {
        list.clear();
        Cursor query = MainActivity.db.rawQuery("SELECT * FROM alarms", null);
        while (query.moveToNext()) {
            int id = query.getInt(0);
            String description = query.getString(1);
            int penalty = query.getInt(2);
            String time = query.getString(3);
            boolean isEnabled = query.getInt(4) == 1;
            AlarmClock.Repeat repeat = new AlarmClock.Repeat();
            AlarmClock alarmClock = new AlarmClock(this); //TODO: переписать код на фабрику
            alarmClock.setTime(time);
            alarmClock.setStringTime(time);
            alarmClock.setId(id);
            alarmClock.setEnabled(isEnabled);
            alarmClock.setDescription(description);
            alarmClock.setPenalty(penalty);
            alarmClock.putExtras();
            if (alarmClock.isEnabled) {
                alarmClock.setRepeat(repeat);
            }
            list.add(alarmClock);
        }
        sortArray();

    }

    private int generateId() {
        int b = random.nextInt(1000000) + 1;
        Cursor query = MainActivity.db.rawQuery("SELECT alarmId FROM alarms;", null);
        while (query.moveToNext()) {
            int queryId = query.getInt(0);
            if (queryId == b || b == 0) {
                b = random.nextInt(1000000) + 1;
                query.moveToFirst();
            }
        }
        return b;
    }

}
