package com.example.falldetector;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;


import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

interface AccValuesChangedListener {
    void OnAccValuesChanged();
}

public class AccTracker extends Service implements SensorEventListener{
    public enum AccelerometerAxis {
        X,
        Y,
        Z,
        ACCELERATION
    }


    private final Context mContext;
    Float[] values = {0.0f, 0.0f, 0.0f};
    boolean fallDetected = false;
    private static List<AccValuesChangedListener> listeners = new ArrayList<>();

    private SensorManager sensorManager;
    private Sensor accSensor;
    private int detections = 0;

    public static double ACC_THRESHOLD = 12.8; //9.2
    public static double CAV_THRESHOLD = 16;
    public static double CCA_THRESHOLD = 65;

    private List<Map<AccelerometerAxis, Double>> accelerometerValues = new ArrayList<>();

    public AccTracker(Context context) {
        this.mContext = context;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accSensor, 10000, 10000);
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
        if (this.isFallDetected(values[0], values[1], values[2]) ) {
            fallDetected = true;
        } else {
            fallDetected = false;
        }
    }

    private boolean isFallDetected(double x, double y, double z) {
        double acceleration = this.calculateAcceleration(x, y, z);
        this.addAccelerometerValuesToList(x, y, z, acceleration);

        if (acceleration > ACC_THRESHOLD) {
            double angleVariation = this.calculateAngleVariation();
            if (angleVariation > CAV_THRESHOLD) {
                double changeInAngle = this.calculateChangeInAngle();
                if (changeInAngle > CCA_THRESHOLD) {
                    if(detections == 0) {
                        detections = 0;
                        return true;
                    } else {
                        detections++;
                    }
                } else {
                    detections = 0;
                }
            } else {
                detections = 0;
            }
        } else {
            detections = 0;
        }
        return false;
    }
    private void addAccelerometerValuesToList(double x, double y, double z, double acceleration) {
        if(this.accelerometerValues.size() >= 4) {
            this.accelerometerValues.remove(0);
        }
        Map<AccelerometerAxis, Double> map = new HashMap<>();
        map.put(AccelerometerAxis.X, x);
        map.put(AccelerometerAxis.Y, y);
        map.put(AccelerometerAxis.Z, z);
        map.put(AccelerometerAxis.ACCELERATION, acceleration);
        this.accelerometerValues.add(map);
    }


    private double calculateAcceleration(double x, double y, double z) {
        return Math.sqrt((Math.abs(x)*Math.abs(x)) + (Math.abs(y)*Math.abs(y)) + (Math.abs(x)*Math.abs(x)));
    }

    private double calculateAngleVariation() {
        int size = this.accelerometerValues.size();
        if (size < 2){
            return -1;
        }

        Map<AccelerometerAxis, Double> minusTwo = this.accelerometerValues.get(size - 2);
        Map<AccelerometerAxis, Double> minusOne = this.accelerometerValues.get(size - 1);

        double anX = minusTwo.get(AccelerometerAxis.X) * minusOne.get(AccelerometerAxis.X);
        double anY = minusTwo.get(AccelerometerAxis.Y) * minusOne.get(AccelerometerAxis.Y);
        double anZ = minusTwo.get(AccelerometerAxis.Z) * minusOne.get(AccelerometerAxis.Z);
        double an = anX + anY + anZ;

        double anX0 = Math.pow(minusTwo.get(AccelerometerAxis.X), 2);
        double anY0 = Math.pow(minusTwo.get(AccelerometerAxis.Y), 2);
        double anZ0 = Math.pow(minusTwo.get(AccelerometerAxis.Z), 2);
        double an0 = Math.sqrt(anX0 + anY0 + anZ0);

        double anX1 = Math.pow(minusOne.get(AccelerometerAxis.X), 2);
        double anY1 = Math.pow(minusOne.get(AccelerometerAxis.Y), 2);
        double anZ1 = Math.pow(minusOne.get(AccelerometerAxis.Z), 2);
        double an1 = Math.sqrt(anX1 + anY1 + anZ1);

        double a = an / (an0 * an1);

        return Math.acos(a) * (180 / Math.PI);
    }

    private double calculateChangeInAngle() {
        int size = this.accelerometerValues.size();
        if (size < 4){
            return -1;
        }
        Map<AccelerometerAxis, Double> first = this.accelerometerValues.get(0);
        Map<AccelerometerAxis, Double> third = this.accelerometerValues.get(3);

        double aX = first.get(AccelerometerAxis.X) * third.get(AccelerometerAxis.X);
        double aY = first.get(AccelerometerAxis.Y) * third.get(AccelerometerAxis.Y);
        double aZ = first.get(AccelerometerAxis.Z) * third.get(AccelerometerAxis.Z);

        double a0 = aX + aY + aZ;

        aX = Math.pow(aX, 2);
        aY = Math.pow(aY, 2);
        aZ = Math.pow(aZ, 2);
        double a1 = (Math.sqrt(aX) + Math.sqrt(aY) + Math.sqrt(aZ));

        return Math.acos(a0 / a1) * (180 / Math.PI);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
