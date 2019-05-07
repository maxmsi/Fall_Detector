package com.example.falldetector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;

public class Alarm extends AppCompatActivity {


    private Button noHelpButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this );

        final Timer timer = new Timer();
        // schedule to send smses after delay
        // TODO: timer.schedule(task, delay);

        final Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if(sharedPreferences.getBoolean("vibrationEnabled", true)) {
            vib.vibrate(Integer.parseInt(sharedPreferences.getString("timeDelay", "3")) * 1000);
        }
        //if sound enabled play alarm
        if(true) {
            // TODO: Play sound
        }

        final ProgressBar mProgressBar=((ProgressBar)findViewById(R.id.alarmLeftBar));
        final int maxAlarmTime = Integer.parseInt(sharedPreferences.getString("timeDelay", "3")) * 1000;
        mProgressBar.setMax(maxAlarmTime);
        final CountDownTimer mCountDownTimer;

        mProgressBar.setProgress(0);
        mCountDownTimer=new CountDownTimer(maxAlarmTime,100) {
            int i=0;
            @Override
            public void onTick(long millisUntilFinished) {
                i++;
                mProgressBar.incrementProgressBy(100);

            }
            @Override
            public void onFinish() {
                i++;
                mProgressBar.setProgress(maxAlarmTime);
            }
        };
        mCountDownTimer.start();

        // TODO: discard alarm by shaking
        findViewById(R.id.noHelopButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vib.cancel();
                timer.cancel();
                mCountDownTimer.cancel();
                Alarm.super.finish();
            }
        });
    }
}


