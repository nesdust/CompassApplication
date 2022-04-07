package com.example.compassapplication;

//import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
//import android.os.Build;
import android.os.Bundle;
//import android.os.VibrationEffect;
//import android.os.Vibrator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayCompassActivity extends AppCompatActivity implements SensorEventListener{

    // compass ImageView
    private ImageView image;

    // The current picture angle from original pos
    private float currentDegree = 0f;

    // The handler for the device sensors
    private SensorManager sensorManager;

    TextView degreeTextView;

    // Using both accelerometer and magnetometer (sensor fusion) for more accurate readings
    // (Also workaround to the deprecation of Sensor.TYPE_ORIENTATION)
    private Sensor accelerometer;
    private Sensor magneticField;

    // Most recent reading of accelermeter sensor
    private float[] prevAccReading = new float[3];
    // Most recent reading of accelermeter sensor
    private float[] prevMagReading = new float[3];

    // rotation matrix and orientation vector used to calc orientation in degrees
    private float[] rotationMatrix = new float[9];
    private float[] orientationVector = new float[3];

    private final static float alpha = 0.10f;

    //private Vibrator vib;
    //private VibrationEffect vibEff; // = (int) EFFECT_TICK;

    private boolean hasMagReading, hasAccReading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_compass);

        // retrives the image view for the compass
        image = (ImageView) findViewById(R.id.compassImageView);

        // TextView that will display the degree
        degreeTextView = (TextView) findViewById(R.id.degreeTextView);

        // initializes the android devices sensor handler/manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Register listeners for accelerometer and magnetometer sensor
        sensorManager.registerListener(this, accelerometer,  SensorManager.SENSOR_DELAY_GAME);
        //sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);
        //SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);

        sensorManager.registerListener(this, magneticField,  SensorManager.SENSOR_DELAY_GAME);
        //sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);
        //SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);

        hasMagReading = false;
        hasAccReading = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // https://www.raywenderlich.com/10838302-sensors-tutorial-for-android-getting-started
        // but converted to java

        if(event == null){
            return;
        }

        // Makes deepcopy of the sensor event values to either prevAccReading eller prevMagReading
        // now instead low pass filter, inspo from https://developer.android.com/reference/android/hardware/SensorEvent#values

        // ACCELEROMETER READING
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            System.arraycopy(event.values, 0, prevAccReading,0, event.values.length);
            if(!hasAccReading) {
                System.arraycopy(event.values, 0, prevAccReading,0, event.values.length);
                hasAccReading = true;
            }else {
                float [] valuesClone = event.values.clone();
                for(int i = 0; i < valuesClone.length; i++) {
                    prevAccReading[i] = prevAccReading[i] + alpha * (valuesClone.clone()[i] - prevAccReading[i]);
                }
            }

        // MAGNETOMETER READING
        } else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            if(!hasMagReading) {
                System.arraycopy(event.values, 0, prevMagReading, 0, event.values.length);
                hasMagReading = true;
            }else{
                float [] valuesClone = event.values.clone();
                for(int i = 0; i < valuesClone.length; i++) {
                    prevMagReading[i] = prevMagReading[i] + alpha * (valuesClone[i] - prevMagReading[i]);
                }
            }
        }
        updateAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        sensorManager.unregisterListener(this);
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, magneticField);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // activate listeners for the sensors accelerometer and magneticField
        sensorManager.registerListener(this, accelerometer,  SensorManager.SENSOR_DELAY_GAME);
        //sensorManager.registerListener(this, accelerometer,  SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);
        //sensorManager.registerListener(this, accelerometer,  SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);

        sensorManager.registerListener(this, magneticField,  SensorManager.SENSOR_DELAY_GAME);
        //sensorManager.registerListener(this, magneticField,  SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);
        //sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);
    }


    private void updateAnimation(){
        // https://www.raywenderlich.com/10838302-sensors-tutorial-for-android-getting-started
        // but converted to java

        if(hasMagReading && hasAccReading){
            SensorManager.getRotationMatrix(rotationMatrix, null, prevAccReading, prevMagReading);

            //gets and updates orientation vector
            float[] orientation = SensorManager.getOrientation(rotationMatrix, orientationVector);

            //return the angle around the z-axis rotated (Azizmuth)
            float degree = (float) (Math.toDegrees(orientation[0]) + 360) % 360;
            //float degree = (float) (((orientation[0] * 180) / Math.PI) + 180);
            degree = Math.round(degree);

            // taken from https://www.codespeedy.com/simple-compass-code-with-android-studio/
            // to animate rotation

            // setText complains when using string concatenation directly in func call
            String newDegStr = "Heading: " + degree + " degrees";
            degreeTextView.setText(newDegStr);

            // create a rotation animation (reverse turn degree degrees)
            RotateAnimation ra = new RotateAnimation(
                    currentDegree,
                    -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            // how long the animation will take place
            ra.setDuration(210);
            // set the animation after the end of the reservation status
            ra.setFillAfter(true);

            // Start the animation
            image.startAnimation(ra);

            currentDegree = -degree;
        } else {
            System.out.println("No valid reading");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Method not used but needed when implementing SensorEventListener
    }
}