package vojtele1.gameofflags.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

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

    /**
     * Vraci true/false, pokud je/neni zaznamenan pohyb
     * @return
     */
    public boolean pohyb() {
        if (krok > 1.0) {
            activityRunning = false;
            return true;
        }

        activityRunning = true;
        return false;
    }
    public void enableStepDetector(boolean enable) {
        if (enable) {
            // kontrola jestli zarizeni ma sensor
            if (stepDetector != null) {
                sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_UI);
            }
        } else {
            sensorManager.unregisterListener(this, stepDetector);
            krok = 0;
        }
    }
}
