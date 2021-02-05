package com.example.covid19selfevaluationapp.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import com.example.covid19selfevaluationapp.MainActivity;

public class AccelerometerService extends Service implements SensorEventListener {

    private static final String TAG = "AccelerometerService";
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;

    private ResultReceiver resultReceiver;


    public AccelerometerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, " ****************************** onDestroy: Unregister the sensor **********************************");
        sensorManager.unregisterListener(this,accelerometerSensor);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // get sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // get sensor
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // register listener
        sensorManager.registerListener(this,accelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);

        resultReceiver = intent.getParcelableExtra(Intent.EXTRA_RESULT_RECEIVER);

        return START_NOT_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.d(TAG, "onSensorChanged: "+ " X = "+ event.values[0]+ " Y = " + event.values[1]+ " Z = " + event.values[2]  );
        //pass results back to calling activity
        Bundle bundle = new Bundle();
        bundle.putString("xValue", String.valueOf(event.values[0]));
        bundle.putString("yValue", String.valueOf(event.values[1]));
        bundle.putString("zValue", String.valueOf(event.values[2]));
        resultReceiver.send(MainActivity.RESULT_OK,bundle);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}