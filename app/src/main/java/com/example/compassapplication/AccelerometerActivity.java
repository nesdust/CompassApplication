package com.example.compassapplication;


import static android.os.VibrationEffect.EFFECT_TICK;
import static android.os.VibrationEffect.EFFECT_HEAVY_CLICK;
import static android.os.VibrationEffect.EFFECT_CLICK;
import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;
import static android.os.VibrationEffect.EFFECT_DOUBLE_CLICK;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.ImageView;
import android.widget.TextView;

public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener {

    // ----- SENSOR -------
    // The handler for the device sensors
    private SensorManager sensorManager;

    private Sensor accelerometer;

    // Most recent reading of accelermeter sensor
    private float[] accelerometerReading = new float[3];

    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];

    // ----- TextViews -------
    TextView accValuesTextView;

    // ----- ImageViews -------
    ImageView androidImageView;

    private float pos, prevPos;

    // ----- Vibrator -------
    private Vibrator vib;
    private VibrationEffect vibEff; // = (int) EFFECT_TICK;

    private boolean hasVibedRight = false;
    private boolean hasVibedLeft = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        accValuesTextView = (TextView) findViewById(R.id.accValuesTextView);
        androidImageView = (ImageView) findViewById(R.id.androidIcon);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

        pos = 0;
        prevPos = 0;

        vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        // Fix: use one of the alternatives to handle OS version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibEff = VibrationEffect.createOneShot(50, 128);
        }else{
            vibEff = VibrationEffect.createOneShot(50, 128); //(VibrationEffect) EFFECT_HEAVY_CLICK;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSensorChanged(SensorEvent event) {

        //sensor readings rounded to two decimal points
        accelerometerReading[0] = (float) (Math.round(event.values[0]*100.0)/100.0);
        accelerometerReading[1] = (float)(Math.round(event.values[1]*100.0)/100.0);
        accelerometerReading[2] = (float) (Math.round(event.values[2]*100.0)/100.0);

        String newValues = "X: " + accelerometerReading[0] + "\nY: " + accelerometerReading[1] + "\nZ: " + accelerometerReading[2];
        accValuesTextView.setText(newValues);

        /*
         * bounds:
         * right/left: +-450
         * up: -300
         * down: 700
         */
        prevPos = pos;
        if (accelerometerReading[0] < -1) {
            if(pos < 450){ // hasn´t hit wall, move in right direction
                pos += 10;
                hasVibedLeft = false;
            }else if(pos >= 450 && !hasVibedRight){ // hit wall -> vibrate (one time untill move right)
                hasVibedRight = true;
                //vib.vibrate(EFFECT_HEAVY_CLICK);
                vib.vibrate(vibEff);
            }
        } else if(accelerometerReading[0] > 1) {
            if(pos > -450) { // hasn´t hit wall move in Left direction
                pos -= 10;
                hasVibedRight = false;
            }else if(pos <= -450 && !hasVibedLeft){ // hit wall -> vibrate (one time untill move right)
                hasVibedLeft = true;
                //vib.vibrate(EFFECT_HEAVY_CLICK);
                vib.vibrate(vibEff);
            }
        }

        // don´t animate if not moved
        if(prevPos != pos){
            ObjectAnimator animation = ObjectAnimator.ofFloat(androidImageView, "translationX", pos);
            animation.setDuration(10);
            animation.start();
        }
        /*
        final float alpha = 0.8f;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        linear_acceleration[0] = ((float) (Math.round((event.values[0] - gravity[0])*100)/100.0));
        linear_acceleration[1] = ((float) (Math.round((event.values[1] - gravity[1])*100)/100.0));
        linear_acceleration[2] = ((float) (Math.round((event.values[2] - gravity[2])*100)/100.0));
        //String newValues = "X: " + linear_acceleration[0] + "\nY: " + linear_acceleration[1] + "\nZ: " + linear_acceleration[2];

         */
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // activate listeners for the sensors accelerometer and magneticField
        sensorManager.registerListener(this, accelerometer,  SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}