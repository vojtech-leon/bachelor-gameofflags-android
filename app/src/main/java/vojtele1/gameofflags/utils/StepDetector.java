package vojtele1.gameofflags.utils;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

/**
 * Created by Leon on 30.03.2016.
 */
public class StepDetector implements SensorEventListener {

    Context context;
    private SensorManager sensorManager;
    private Sensor stepDetector;
    private double krok;

    boolean activityRunning;

    public StepDetector(Context context) {
        this.context = context;

        activityRunning = true;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        // kontrola jestli zarizeni ma sensor
        if (stepDetector != null) {
            sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_UI);
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        final int type = event.sensor.getType();
        if (activityRunning) {
            if (type == Sensor.TYPE_STEP_DETECTOR) {
                krok = krok + event.values[0];
                System.out.println("Krok! " + krok);
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public boolean pohyb() {
        if (krok != 0.0) {
            System.out.println("pohnul se");
            activityRunning = false;
            krok = 0;
            return true;
        }

        activityRunning = true;
        return false;
    }
}
