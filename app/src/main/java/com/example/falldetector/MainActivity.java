package com.example.falldetector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

        // TEST FOR GPS
        gpsTracker = new GPSTracker(this);
        ((TextView) findViewById(R.id.gpsLocationField)).setText(gpsTracker.getLatitude() + " " + gpsTracker.getLongitude());

        // TEST FOR accelerometer
        accTracker = new AccTracker(this);
        accTracker.addAccValueListener(new AccValuesChangedListener() {
            @Override
            public void OnAccValuesChanged() {
                ((TextView) findViewById(R.id.sensorsDataField)).setText(accTracker.values[0] + " " + accTracker.values[1] + " " + accTracker.values[2]);
            }
        });


        //TODO: send test sms notification to self
        findViewById(R.id.smsNotificationTest).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });
    }
}
