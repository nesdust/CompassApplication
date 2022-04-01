package com.example.compassapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayCompassActivity extends AppCompatActivity implements SensorEventListener{

    // compass ImageView
    private ImageView image;

    // The current picture angle from original pos
    private float currentDegree = 0F;

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
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);
                //SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);
                //SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event == null){
            return;
        }

        // Makes deepcopy of the sensor event values to either prevAccReading eller prevMagReading
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            System.arraycopy(event.values, 0, prevAccReading,0, event.values.length);
        } else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, prevMagReading,0, event.values.length);
        }

        updateAnimation();
    }

    private void updateAnimation(){

        boolean valid_reading = SensorManager.getRotationMatrix(rotationMatrix, null, prevAccReading, prevMagReading);

        if(valid_reading){
            //gets and updates orientation vector
            float[] orientation = SensorManager.getOrientation(rotationMatrix, orientationVector);

            //return the angle around the z-axis rotated (Azizmuth)
            float degree = (float) (Math.toDegrees(orientation[0]) + 360) % 360;
            degree = Math.round(degree * 100) / 100;

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
        sensorManager.registerListener(this, accelerometer,  SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);
                //SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);
                //SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);
        // SENSOR_DELAY_GAME
        // SENSOR_DELAY_FASTEST
        // SENSOR_DELAY_NORMAL
        // SENSOR_DELAY_UI
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Method not used but needed when implementing SensorEventListener
    }
}