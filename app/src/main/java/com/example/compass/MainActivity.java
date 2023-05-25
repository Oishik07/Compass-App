package com.example.compass;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    private TextView textView,strength,power;
    private ImageView imageView;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor,magnetometerSensor;

    private float[] lastAccelerometer=new float[3];
    private float[] lastMagnetometer=new float[3];
    private float[] rotationMatrix=new float[9];
    private float[] orientation=new float[3];

    boolean isLastAccelerometerArrayCopied=false;
    boolean isLastMagnetometerArrayCopied=false;

    long lastUpdatedTime=0;
    float currentDegree=0f;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textView=findViewById(R.id.value);
        imageView=findViewById(R.id.compass_image);
        strength=findViewById(R.id.textView2);
        power=findViewById(R.id.textView3);

        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor=sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        
        boolean f=accelerometerSensor.isWakeUpSensor();
        power.setText("Power consumed by sensors : "+(accelerometerSensor.getPower()+magnetometerSensor.getPower())+" µA");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        if(sensorEvent.sensor==accelerometerSensor)
        {
            System.arraycopy(sensorEvent.values,0,lastAccelerometer,0,sensorEvent.values.length);
            isLastAccelerometerArrayCopied=true;
        }
        else if(sensorEvent.sensor==magnetometerSensor)
        {
            System.arraycopy(sensorEvent.values,0,lastMagnetometer,0,sensorEvent.values.length);
            isLastMagnetometerArrayCopied=true;

            float azimuth=Math.round(sensorEvent.values[0]);
            float pitch=Math.round(sensorEvent.values[1]);
            float roll=Math.round(sensorEvent.values[2]);

            double tesla=Math.sqrt((azimuth*azimuth) + (pitch*pitch) + (roll*roll));

            String s=String.format("%.1f",tesla);
            strength.setText("Magnetic field strength : "+ s+" μT");
        }

        if(isLastAccelerometerArrayCopied && isLastMagnetometerArrayCopied && System.currentTimeMillis()-lastUpdatedTime>23)
        {
            SensorManager.getRotationMatrix(rotationMatrix,null,lastAccelerometer,lastMagnetometer);
            SensorManager.getOrientation(rotationMatrix,orientation);

            float azimuthInRadian=orientation[0];
            float azimuthInDegree=(float)Math.toDegrees(azimuthInRadian);

            RotateAnimation rotateAnimation=new RotateAnimation(currentDegree,-azimuthInDegree, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            rotateAnimation.setDuration(23);
            rotateAnimation.setFillAfter(true);
            imageView.startAnimation(rotateAnimation);

            currentDegree=-azimuthInDegree;
            lastUpdatedTime=System.currentTimeMillis();

            int x=(int)azimuthInDegree;
            String direction="";

            if(x>=-22 && x<=22)
                direction="N";
            else if(x>=23 && x<=67)
                direction="NE";
            else if(x>=68 && x<=112)
                direction="E";
            else if(x>=113 && x<=157)
                direction="SE";
            else if(x>=158 || x<=-158)
                direction="S";
            else if(x>=-157 && x<=-113)
                direction="SW";
            else if(x>=-112 && x<=-68)
                direction="W";
            else if(x>=-67 && x<=-23)
                direction="NW";

            textView.setText(x+"° "+direction);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this,accelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,magnetometerSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this,accelerometerSensor);
        sensorManager.unregisterListener(this,magnetometerSensor);
    }
}