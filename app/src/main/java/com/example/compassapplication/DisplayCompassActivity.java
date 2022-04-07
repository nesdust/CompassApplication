package com.example.compassapplication;

//import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
//import android.os.Build;
import android.media.Image;
import android.os.Bundle;
//import android.os.VibrationEffect;
//import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DisplayCompassActivity extends AppCompatActivity implements SensorEventListener{

    //private RelativeLayout relativeLayout;

    // compass ImageView
    private ImageView image;

    // The current picture angle from original pos
    //private float currentDegree = 0f;
    private int currentDegree = 0;

    // The handler for the device sensors
    private SensorManager sensorManager;

    TextView degreeTextView;

    ImageView santaImageView;
    TextView santaTextView;
    private boolean headingSanta;

    // Using both accelerometer and magnetometer (sensor fusion) for more accurate readings
    // (Also workaround to the deprecation of Sensor.TYPE_ORIENTATION)
    private Sensor accelerometer;
    private Sensor magneticField;

    private float[] gravity = new float[3];
    // Most recent reading of accelermeter sensor
    private float[] prevAccReading = new float[3];
    // Most recent reading of accelermeter sensor
    private float[] prevMagReading = new float[3];

    // rotation matrix and orientation vector used to calc orientation in degrees
    private float[] rotationMatrix = new float[9];
    private float[] orientationVector = new float[3];

    private float[] sumOrientation = new float[3];

    private final static float alpha = 0.15f;

    //private Vibrator vib;
    //private VibrationEffect vibEff; // = (int) EFFECT_TICK;

    private boolean hasMagReading, hasAccReading;

    private String newHeading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_compass);

        //relativeLayout = findViewById(R.id.compassRelativeLayout);
        //relativeLayout.setBackgroundColor(Color.RED);

        // retrives the image view for the compass
        image = (ImageView) findViewById(R.id.compassImageView);

        // TextView that will display the degree
        degreeTextView = (TextView) findViewById(R.id.degreeTextView);

        // TextView & ImageView that will display santa text if newHeading == "N"
        santaTextView = (TextView) findViewById(R.id.santaTextView);
        santaTextView.setVisibility(View.INVISIBLE);
        santaImageView = (ImageView) findViewById(R.id.santaHatImageView);
        santaImageView.setVisibility(View.INVISIBLE);
        headingSanta = false;

        // initializes the android devices sensor handler/manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Register listeners for accelerometer and magnetometer sensor
        sensorManager.registerListener(this, accelerometer,  SensorManager.SENSOR_DELAY_GAME);
        //sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);

        sensorManager.registerListener(this, magneticField,  SensorManager.SENSOR_DELAY_GAME);
        //sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME);

        hasMagReading = false;
        hasAccReading = false;

        newHeading = "";
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
                float[] valuesClone = event.values.clone();
                for (int i = 0; i < valuesClone.length; i++) {
                    prevAccReading[i] = prevAccReading[i] + alpha * (valuesClone[i] - prevAccReading[i]);
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
        if(hasMagReading && hasAccReading) {
            // update rotationMatrix
            SensorManager.getRotationMatrix(rotationMatrix, null, prevAccReading, prevMagReading);
            // get orientations
            float[] orientations = SensorManager.getOrientation(rotationMatrix, orientationVector);
            //convert the angle around the z-axis rotated (Azizmuth) to degrees
            int degree = (int) (Math.toDegrees(orientations[0]) + 360) % 360;
            degree = Math.round(degree);

            //update textView
            updateHeadingText(degree);

            // update santa text if heading is north or hide text if not
            checkSantaHeading();

            // animate rotation of compass
            updateAnimation(degree);

            currentDegree = -degree;
        }
    }

    private void updateHeadingText(int degree) {
        updateDirection(degree);

        String newDegStr = "Heading: " + degree + " " + newHeading;
        degreeTextView.setText(newDegStr);
    }

    private void updateAnimation(int degree){

        // taken from https://www.codespeedy.com/simple-compass-code-with-android-studio/
        // to animate rotation
        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        //int t = Math.abs(currentDegree - degree);
        int degDiff = (currentDegree - degree);
        int t = Math.abs(degDiff * 100);
        ra.setDuration(t);
        // set the animation after the end of the reservation status
        //ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
    }


    // changes background color when
    private void updateBackground(){

    }

    private void checkSantaHeading() {
        if(newHeading == "N" && !headingSanta) {
            //santaTextView.setText("Lets go and see santa");
            santaTextView.setVisibility(View.VISIBLE);
            santaImageView.setVisibility(View.VISIBLE);
            headingSanta = true;
        }else if(newHeading != "N" && headingSanta){
            santaImageView.setVisibility(View.INVISIBLE);
            santaTextView.setVisibility(View.INVISIBLE);
            //santaTextView.setText("");
            headingSanta = false;
        }
    }

    // returns the current direction
    private void updateDirection(int degree) {

        if (degree >= 350 || degree <= 10) {
            newHeading = "N";
        }
        else if (degree < 350 && degree > 280) {
            newHeading = "NW";
        }
        else if (degree <= 280 && degree > 260) {
            newHeading = "W";
        }
        else if (degree <= 260 && degree > 190) {
            newHeading = "SW";
        }
        else if (degree <= 190 && degree > 170) {
            newHeading = "S";
        }
        else if (degree <= 170 && degree > 100) {
            newHeading = "SE";
        }
        else if (degree <= 100 && degree > 80) {
            newHeading = "E";
        }
        else if (degree <= 80 && degree > 10) {
            newHeading = "NE";
        }
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Method not used but needed when implementing SensorEventListener
    }
}