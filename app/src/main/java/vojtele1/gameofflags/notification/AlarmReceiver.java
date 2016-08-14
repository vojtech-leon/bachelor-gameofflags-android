package vojtele1.gameofflags.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

import vojtele1.gameofflags.utils.C;

/**
 * Created by Leon on 14.05.2016.
 */
public class AlarmReceiver extends BroadcastReceiver {

    Context context;

    int scanCounter;
    WifiManager wifiManager;
    /**
     * Umozni nacitat a ukladat hodnoty do pameti
     */
    private SharedPreferences sharedPreferences;
    /**
     * V jake frakci byl hrac naposled
     */
    private String playerFraction;

    Notification notification;

    AlarmManager alarmManager;
    Intent alarmIntent;
    PendingIntent alarmPendingIntent, alarmPendingIntent2, alarmPendingIntent3, alarmPendingIntent4;
    private boolean notificationAdded;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // Retrieve an instance of the SharedPreferences object.
        sharedPreferences = context.getSharedPreferences(C.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);

        // Get the value of playerFraction from SharedPreferences. Set to 0 (chyba) as a default.
        playerFraction = sharedPreferences.getString(C.PLAYER_FRACTION, "0");

        // Get the value of notificationAdded from SharedPreferences. Set to false as a default.
        notificationAdded = sharedPreferences.getBoolean(C.NOTIFICATION_ADDED_KEY, false);

        // pokud byl alarm zaply, zapne ho i po restartu zarizeni
        if ((Intent.ACTION_BOOT_COMPLETED).equals(intent.getAction()) && notificationAdded) {
            setAlarms(context);
            Log.i(C.LOG_ALARMRECEIVER, "nastaveno zapinani notifikaci i po bootu");
        }
        else {
            notification = new Notification(context);
            if (wifiManager.isWifiEnabled()) {
                wifiManager.startScan();
                List<ScanResult> results = wifiManager.getScanResults();
                for (ScanResult result : results) {
                    if (C.MAC_EDUROAM.contains(result.BSSID)) {
                        Log.i(C.LOG_ALARMRECEIVER, "nalezena spravna mac");
                        // posle notifikace stejne i pro geofencing
                        notification.sendNotification();
                        break;
                    } else if (results.size() == scanCounter) {
                        Log.i(C.LOG_ALARMRECEIVER, "neni tu spravna mac");
                    } else {
                        Log.i(C.LOG_ALARMRECEIVER, "nespravna mac, pokracuji v prochazeni pole");
                    }
                    scanCounter++;
                }
            } else {
                Log.i(C.LOG_ALARMRECEIVER, "neni zapla wifi, nelze overit okolni vysilace");
            }
        }
    }
    public void setAlarms(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(context, 1, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmPendingIntent2 = PendingIntent.getBroadcast(context, 2, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmPendingIntent3 = PendingIntent.getBroadcast(context, 3, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmPendingIntent4 = PendingIntent.getBroadcast(context, 4, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.
                INTERVAL_DAY, alarmPendingIntent);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.
                INTERVAL_DAY, alarmPendingIntent2);
        calendar.set(Calendar.HOUR_OF_DAY, 15);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.
                INTERVAL_DAY, alarmPendingIntent3);
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.
                INTERVAL_DAY, alarmPendingIntent4);
        // pro testovani kazdou minutu
        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, 1000*60, alarmPendingIntent2);
    }
    public void removeAlarms(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(context, 1, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmPendingIntent2 = PendingIntent.getBroadcast(context, 2, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmPendingIntent3 = PendingIntent.getBroadcast(context, 3, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmPendingIntent4 = PendingIntent.getBroadcast(context, 4, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        alarmManager.cancel(alarmPendingIntent);
        alarmManager.cancel(alarmPendingIntent2);
        alarmManager.cancel(alarmPendingIntent3);
        alarmManager.cancel(alarmPendingIntent4);
    }

}
