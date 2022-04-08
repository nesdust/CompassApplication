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

    private float yPos, yPrevPos = 0.0f;
    private float xPos, xPrevPos = 0.0f;

    // ----- Vibrator -------
    private Vibrator vib;
    private VibrationEffect vibEff; // = (int) EFFECT_TICK;

    private boolean hasVibedRight,hasVibedLeft, hasVibedUp, hasVibedDown = false;

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

        vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        // Fix: use one of the alternatives to handle OS version
        vibEff = VibrationEffect.createOneShot(50, 128);

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

        updateKord(accelerometerReading[0], accelerometerReading[1]);

        // don´t animate if not moved
        if(xPrevPos != xPos || yPrevPos != yPos){
            ObjectAnimator xAnimation = ObjectAnimator.ofFloat(androidImageView, "translationX", xPos);
            ObjectAnimator yAnimation = ObjectAnimator.ofFloat(androidImageView, "translationY", yPos);
            xAnimation.setDuration(10);
            yAnimation.setDuration(10);
            xAnimation.start();
            yAnimation.start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateKord(float newX, float newY) {
        /*
         * bounds:
         * right/left: +-450
         * up: -300
         * down: 700
         */
        xPrevPos = xPos;
        yPrevPos = yPos;
        if (newX < -1) {
            if(xPos < 450){ // hasn´t hit wall, move in right direction
                xPos += 10;
                hasVibedLeft = false;
            }else if(xPos >= 450 && !hasVibedRight){ // hit wall -> vibrate (one time untill move right)
                hasVibedRight = true;
                //vib.vibrate(EFFECT_HEAVY_CLICK);
                vib.vibrate(vibEff);
            }
        }else if(newX > 1) {
            if(xPos > -450) { // hasn´t hit wall move in Left direction
                xPos -= 10;
                hasVibedRight = false;
            }else if(xPos <= -450 && !hasVibedLeft){ // hit wall -> vibrate (one time untill move right)
                hasVibedLeft = true;
                //vib.vibrate(EFFECT_HEAVY_CLICK);
                vib.vibrate(vibEff);
            }
        }
        if (newY > 1) {
            if(yPos < 700){ // hasn´t hit wall, move in right direction
                yPos += 10;
                hasVibedDown = false;
            }else if(yPos >= 700 && !hasVibedUp){ // hit wall -> vibrate (one time untill move right)
                hasVibedUp = true;
                vib.vibrate(vibEff);
            }
        } else if(newY < -1) {
            if(yPos > -700) { // hasn´t hit wall move in Left direction
                yPos -= 10;
                hasVibedUp = false;
            }else if(yPos <= -700 && !hasVibedDown){ // hit wall -> vibrate (one time untill move right)
                hasVibedDown = true;
                vib.vibrate(vibEff);
            }
        }
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