package com.example.falldetector;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

interface AccValuesChangedListener {
    public void OnAccValuesChanged();
}

public class AccTracker extends Service implements SensorEventListener{
    private final Context mContext;
    Float[] values = {0.0f, 0.0f, 0.0f};
    private static List<AccValuesChangedListener> listeners = new ArrayList<AccValuesChangedListener>();

    private SensorManager sensorManager;
    private Sensor accSensor;

    public AccTracker(Context context) {
        this.mContext = context;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public Float[] getValues() {
        return values;
    }

    public void setValues(Float[] values) {
        this.values = values;

        for (AccValuesChangedListener l : listeners) {
            l.OnAccValuesChanged();
        }
    }

    public void addAccValueListener(AccValuesChangedListener l) {
        listeners.add(l);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        this.setValues(new Float[] {event.values[0], event.values[1], event.values[2]});
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
