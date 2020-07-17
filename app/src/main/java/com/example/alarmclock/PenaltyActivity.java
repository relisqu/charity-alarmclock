package com.example.alarmclock;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.alarmclock.MainActivity.db;

public class PenaltyActivity extends Activity {
    String date = "";
    String moneyValue = "";
    int id;
    private int[] btn_id = {R.id.logo1, R.id.logo2, R.id.logo3};

    public static void buttonEffect(final View button) {
        button.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (button.isFocused()) {
                    v.getBackground().setColorFilter(0x9d7dbbb9, PorterDuff.Mode.SRC_ATOP);
                } else {
                    v.getBackground().clearColorFilter();
                }
                v.invalidate();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Cursor cursor = db.rawQuery("select * from penalty", null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String dateString = cursor.getString(1);
                try {
                    Date neededDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(dateString);
                    assert neededDate != null;
                    if (neededDate.before(new Date())) {
                        this.date = date;
                        id = cursor.getInt(0);
                        Cursor alarmCursor = db.rawQuery("select * from alarms", null);
                        if (alarmCursor.getCount() > 0) {
                            while (alarmCursor.moveToNext()) {
                                if (alarmCursor.getInt(0) == id) {
                                    moneyValue = alarmCursor.getString(2);
                                }
                            }
                        }
                        if (!moneyValue.equals("")) {
                            break;
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        setContentView(R.layout.penalty_activity);
        if (id > 0) {
            TextView penaltyMoneyView = findViewById(R.id.penaltyMoneyView);
            TextView penaltyTimeView = findViewById(R.id.penaltyTimeView);
            penaltyMoneyView.setText(String.format("%s %s₽", getResources().getString(R.string.penalty_amount_warning), moneyValue));
            penaltyTimeView.setText(String.format("%s %s", getResources().getString(R.string.alarm_failed_message), date.split(" ")[1]));
        }
        Button payPenaltyButton = findViewById(R.id.payPenaltyButton);
        payPenaltyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.execSQL("delete from penalty");
                startActivity(new Intent(PenaltyActivity.this, MainActivity.class));
                PenaltyActivity.this.finish();
            }
        });
        Button logo1 = findViewById(R.id.logo1);//TODO: после получения тз от миши удалить заглушку и переписать на нормальную систему
        Button logo2 = findViewById(R.id.logo2);
        Button logo3 = findViewById(R.id.logo3);
        buttonEffect(logo1);
        buttonEffect(logo2);
        Button[] btn = new Button[3];
        for (int i = 0; i < btn_id.length; i++) {
            btn[i] = findViewById(btn_id[i]);
            buttonEffect(logo3);
        }
        btn[0].requestFocus();

    }
}
