package com.example.falldetector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class Alarm extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this );

        //if vibration enabled vibrate
        final Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if(sharedPreferences.getBoolean("vibrationEnabled", true)) {
            vib.vibrate(Integer.parseInt(sharedPreferences.getString("timeDelay", "3")) * 1000);
        }

        //if sound enabled play alarm
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.sound);
        if(sharedPreferences.getBoolean("soundEnabled", true)) {
            mp.setLooping(true);
            mp.start();
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
                if(sharedPreferences.getBoolean("smsEnabled", true)) {
                    for(int j = 1 ; j <= 3 ; j++) {
                        sendSMS(j);
                    }
                }
                mp.stop();
                mp.release();
                vib.cancel();
                Alarm.super.finish();
            }
        };
        mCountDownTimer.start();

        // TODO: discard alarm by shaking
        findViewById(R.id.noHelpButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vib.cancel();
                mCountDownTimer.cancel();
                mp.stop();
                mp.release();
                Alarm.super.finish();
            }
        });
    }

    void sendSMS(int id) {
        SmsManager smsManager = SmsManager.getDefault();
        String phoneNumber = sharedPreferences.getString("contact" + Integer.toString(id), "");
        Log.v("PhoneNumber", phoneNumber);
        if(phoneNumber.equals("")) {
            return;
        }
        GPSTracker gpsTracker = new GPSTracker(this);

        String testMessage = "I HAVE FALLEN AND I CAN'T GET UP\n"
                + "SENDER: " + sharedPreferences.getString("signature", "NOT SET") + "\n"
                + "Location: ";
        if(sharedPreferences.getBoolean("sendGpsData", true)) {
            testMessage += "lat: " + gpsTracker.getLatitude() + " lon: " + gpsTracker.getLongitude();
        } else {
            testMessage += "NOT ALLOWED";
        }
        try {
            smsManager.sendTextMessage(phoneNumber,null, testMessage,null,null);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}


