package com.example.servicesexample;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    private static final String TAG = "ExampleJobService";
    private EditText editTextInput;
    public static final String REF_ID = "inputExtra";

    private TextView mTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextInput = findViewById(R.id.edit_text_input);

        mTextView = findViewById(R.id.alarm_txt);
        Button timePicker = findViewById(R.id.open_picker_btn);
        timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "time picker");
            }
        });

        Button cancelAlarm = findViewById(R.id.cancel_alarm_btn);
        cancelAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlarm();
            }
        });

    }

    public void startService(View v) {
        String input = editTextInput.getText().toString();

        Intent serviceIntent = new Intent(this, ExampleService.class);
        serviceIntent.putExtra(REF_ID , input);

        startService(serviceIntent);
    }

    public void stopService(View v) {
        Intent serviceIntent = new Intent(this, ExampleService.class);
        stopService(serviceIntent);
    }

    public void enqueueWork(View v) {
        String input = editTextInput.getText().toString();

        Intent serviceIntent = new Intent(this, ExampleJobIntentService.class);
        serviceIntent.putExtra(REF_ID, input);

        ExampleJobIntentService.enqueueWork(this, serviceIntent);
    }

    public void scheduleJob(View v) {
        ComponentName componentName = new ComponentName(this, ExampleJobService.class);
        JobInfo info = new JobInfo.Builder(123 ,componentName)
                .setRequiresCharging(true)  // do our job only if the phone is charging
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED) // job -> wifi Connection only
                .setPersisted(true)  // make the job life even we have rebooted the phone
                .setPeriodic(15 * 60 * 1000) // define how open we need to execute this job [15 Mins]
                .build();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode =
                jobScheduler.schedule(info);

        if (resultCode == JobScheduler.RESULT_SUCCESS ) {
            Log.d(TAG, "Job Scheduled");
        } else {
            Log.d(TAG, "Job Scheduled failed");
        }
    }

    public void cancelJob(View v) {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(123);

        Log.d(TAG, "Job Cancelled");
    }

    //Alarm

    //get the current time s=using Calender
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        updateTimeText(c);
        startAlarm(c);
    }

    //Initialize the Alaram
    private void startAlarm(Calendar c) {

        //Create alarm with broadcast
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this,AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent); //type : RTC -> wake up the device if it Off
    }

    //Update the textView with the time
    private void updateTimeText(Calendar c) {
        String timeTxt = "Alarm set for : ";
        timeTxt += DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());

        mTextView.setText(timeTxt);
    }

    private void cancelAlarm() {

        //Create alarm with broadcast
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this,AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        alarmManager.cancel(pendingIntent);
        mTextView.setText("Alarm Cancelled.");
    }
}
