package com.example.falldetector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private GPSTracker gpsTracker;
    private AccTracker accTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.settingsOpen).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this );

        ((SeekBar)findViewById(R.id.accSeekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                accTracker.ACC_THRESHOLD = 10 + seekBar.getProgress();
                ((TextView)findViewById(R.id.accTextView)).setText("Acceleration: " + accTracker.ACC_THRESHOLD);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        ((SeekBar)findViewById(R.id.localSeekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                accTracker.CAV_THRESHOLD = 13 + seekBar.getProgress();
                ((TextView)findViewById(R.id.localTextView)).setText("Local change in angle: " + accTracker.CAV_THRESHOLD);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        ((SeekBar)findViewById(R.id.globalSeekBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                accTracker.CCA_THRESHOLD = 40 + seekBar.getProgress() * 5;
                ((TextView)findViewById(R.id.globalTextView)).setText("Global change in angle: " + accTracker.CCA_THRESHOLD);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // TEST FOR GPS
        gpsTracker = new GPSTracker(this);
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
                                  @Override
                                  public void run() {
                                      ((TextView) findViewById(R.id.gpsData)).setText("lat: " + gpsTracker.getLatitude() + "\nlon: " + gpsTracker.getLongitude());
                                  }
                              },
//Set how long before to start calling the TimerTask (in milliseconds)
                0,
//Set the amount of time between each execution (in milliseconds)
                10000);

        // TEST FOR accelerometer
        accTracker = new AccTracker(this);
        accTracker.addAccValueListener(new AccValuesChangedListener() {
            @Override
            public void OnAccValuesChanged() {

               ((TextView) findViewById(R.id.sensorsDataField)).setText("X: " + accTracker.values[0] + " Y: " + accTracker.values[1] + " Z: " + accTracker.values[2]);
                if(accTracker.fallDetected) {
                    accTracker.fallDetected = false;
                    Intent myIntent = new Intent(MainActivity.this, Alarm.class);
                    MainActivity.this.startActivity(myIntent);
                }
            }
        });

        // TEST FOR ALARM
        findViewById(R.id.testAlarmButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, Alarm.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

        // TEST FOR SMS
        findViewById(R.id.smsNotificationTest).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SmsManager smsManager = SmsManager.getDefault();
                String phoneNumber = ((TextView)findViewById(R.id.phoneNumberField)).getText().toString();

                String testMessage = "THIS IS A TEST MESSAGE\n"
                                   + "SENDER: " + sharedPreferences.getString("signature", "NOT SET") + "\n"
                                   + "Location: ";
                if(sharedPreferences.getBoolean("sendGpsData", true) == true) {
                    testMessage += "lat: " + gpsTracker.getLatitude() + " lon: " + gpsTracker.getLongitude();
                } else {
                    testMessage += "NOT ALLOWED";
                }
                try {
                    smsManager.sendTextMessage(phoneNumber,null, testMessage,null,null);
                    ((Button)findViewById(R.id.smsNotificationTest)).setEnabled(false);
                    ((ProgressBar)findViewById(R.id.testProgressBar)).setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable()
                                              {
                                                  public void run()
                                                  {
                                                      ((Button)findViewById(R.id.smsNotificationTest)).setEnabled(true);
                                                      ((ProgressBar)findViewById(R.id.testProgressBar)).setVisibility(View.INVISIBLE);
                                                  }
                                              }, 10000000    //Specific time in milliseconds
                    );
                } catch(IllegalArgumentException e) {
                    ((TextView) findViewById(R.id.phoneNumberField)).setError("Wrong Number");
                }

            }
        });
    }
}
