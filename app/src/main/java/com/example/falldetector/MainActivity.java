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
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private GPSTracker gpsTracker;
    private AccTracker accTracker;

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    public  SensorEventListener gyroscopeEventListener;
    private boolean gyroscopeVelocityInFallRange;


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

        // TEST FOR GPS
        gpsTracker = new GPSTracker(this);
        ((TextView) findViewById(R.id.gpsData)).setText("lat: " + gpsTracker.getLatitude() + " lon: " + gpsTracker.getLongitude());

        // TEST FOR accelerometer
        accTracker = new AccTracker(this);
        accTracker.addAccValueListener(new AccValuesChangedListener() {
            @Override
            public void OnAccValuesChanged() {

               ((TextView) findViewById(R.id.sensorsDataField)).setText("X: " + accTracker.values[0] + " Y: " + accTracker.values[1] + " Z: " + accTracker.values[2]);
                if(accTracker.fallDetected && gyroscopeVelocityInFallRange) {
                    accTracker.fallDetected = false;
                    Intent myIntent = new Intent(MainActivity.this, Alarm.class);
                    MainActivity.this.startActivity(myIntent);
                }
            }
        });



        //GYROSCOPE TEST
        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        if(gyroscopeSensor==null)
            ((TextView) findViewById(R.id.sensorsDataField2)).setText("That device does not have an gyroscope sensor");
        else
            ((TextView) findViewById(R.id.sensorsDataField2)).setText("No data yet");



        gyroscopeEventListener=new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                ((TextView) findViewById(R.id.sensorsDataField2)).setText("Orientation X (Roll) :"+ Float.toString(event.values[2]) +"\n"+
                "Orientation Y (Pitch) :"+ Float.toString(event.values[1]) +"\n"+
                        "Orientation Z (Yaw) :"+ Float.toString(event.values[0]));


                //X Velocity
                if(event.values[0]>=3.5f || event.values[0]>= -3.5f)
                    gyroscopeVelocityInFallRange =true;
                else
                    gyroscopeVelocityInFallRange =false;

                //Y Velocity
                if(event.values[0]>=3.0f || event.values[0]>=-3.0f )
                    gyroscopeVelocityInFallRange =true;
                else
                    gyroscopeVelocityInFallRange =false;

                //Z velovity
                if(event.values[0]>=3.0f && event.values[0]>=-3.0f )
                    gyroscopeVelocityInFallRange =true;
                else
                    gyroscopeVelocityInFallRange =false;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sensorManager.registerListener(gyroscopeEventListener,
                gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);



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
