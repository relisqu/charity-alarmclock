package com.example.alarmclock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

class AlarmClockAdapter extends ArrayAdapter<AlarmClock> {
    private LayoutInflater inflater;
    private int layout;
    private List<AlarmClock> alarmClockList;
    private boolean isTheFirstTime=true;

    AlarmClockAdapter(Context context, int resource, List<AlarmClock> alarmClocks) {
        super(context, resource, alarmClocks);
        this.alarmClockList = alarmClocks;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);

    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        final AlarmClock clock = alarmClockList.get(position);
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(this.layout, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.timeView.setText(clock.stringTime);

        viewHolder.enabledSwitch.setChecked(clock.isEnabled);


        viewHolder.enabledSwitch.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                    clock.setEnabled(!clock.isEnabled);
                    String clockInt= clock.isEnabled?"1":"0";
                MainActivity.db.execSQL("UPDATE alarms SET isEnabled=" +clockInt+ " WHERE alarmId = " + clock.id + "");


            }
        });
        viewHolder.dateView.setText(clock.description);
        return convertView;
    }

    private class ViewHolder {
        final TextView timeView, dateView;
        final Switch enabledSwitch;

        ViewHolder(View view) {
            enabledSwitch = view.findViewById(R.id.enable_switch);
            dateView = view.findViewById(R.id.dateView);
            timeView = view.findViewById(R.id.timeView);

        }
    }


}
