package com.example.falldetector;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

interface GyroValuesChangedListener {
    public void OnGyroValuesChanged();
}

public class GyroTracker extends Service implements SensorEventListener {

    private boolean fallDetectedbyGyro;
    private final Context mContext;
    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    public  SensorEventListener gyroscopeEventListener;

    Float[] values = {0.0f, 0.0f, 0.0f};
    private static List<GyroValuesChangedListener> listeners = new ArrayList<>();

    private static  int GYROSCOPE_SAMPLING_PERIOD = 1000;
    private static final float xAngularVelocity = 5.0f;
    private static final float yAngularVelocity = 5.0f;
    private static final float zAngularVelocity = 5.0f;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        this.setValues(new Float[] {event.values[0], event.values[1], event.values[2]});

        Log.v("val1", Float.toString(event.values[0]));
        Log.v("val2", Float.toString(event.values[1]));
        Log.v("val3", Float.toString(event.values[2]));


        values[0] = event.values[0];
        values[1] = event.values[1];
        values[2] = event.values[2];


        if (this.GyroscopeCondition(event.values[0], event.values[1], event.values[2])){

               GYROSCOPE_SAMPLING_PERIOD=50000;
                fallDetectedbyGyro = true;


        } else {
            GYROSCOPE_SAMPLING_PERIOD=1000;
            fallDetectedbyGyro = false;
        }

    }





    @Override

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public boolean getConditionOfGyroscope(){
        return fallDetectedbyGyro;
    }

    public boolean GyroscopeCondition(Float val1,Float val2,Float val3){



        if (val2>=yAngularVelocity){
            return true;
        }
        else if(val1 >= zAngularVelocity || val3 >= xAngularVelocity)
            return true;

        if (val2<=-yAngularVelocity){
            return true;
        }
        else if(val1 <= -zAngularVelocity || val3 <= -xAngularVelocity)
            return true;



        else
        return false;
    }

    public void addGyroValueListener(GyroValuesChangedListener l) {
        listeners.add(l);
    }


    //private List<Map<GyroTracker.GyroscopeVelocity, Double>> accelerometerValues = new ArrayList<>();

    public GyroTracker(Context context) {
        this.mContext = context;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyroscopeSensor, GYROSCOPE_SAMPLING_PERIOD);

    }
    public void setValues(Float[] values) {
        this.values = values;

        for (GyroValuesChangedListener l : listeners) {
            l.OnGyroValuesChanged();
        }
    }






}
