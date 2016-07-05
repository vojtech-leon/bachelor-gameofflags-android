package vojtele1.gameofflags.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.Calendar;
import java.util.List;

import vojtele1.gameofflags.utils.C;

/**
 * Created by Leon on 14.05.2016.
 */
public class AlarmReceiver extends BroadcastReceiver {

    Context context;

    int pocitadloScanu;
    WifiManager wifiManager;
    /**
     * Umozni nacitat a ukladat hodnoty do pameti
     */
    private SharedPreferences mSharedPreferences;
    /**
     * V jake frakci byl hrac naposled
     */
    private String mPlayerFraction;

    Notification notification;


    AlarmManager alarmManager;
    Intent alarmIntent;
    PendingIntent alarmPendingIntent, alarmPendingIntent2, alarmPendingIntent3, alarmPendingIntent4;
    private boolean mNotificationAdded;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // Retrieve an instance of the SharedPreferences object.
        mSharedPreferences = context.getSharedPreferences(C.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);

        // Get the value of mPlayerFraction from SharedPreferences. Set to 0 (chyba) as a default.
        mPlayerFraction = mSharedPreferences.getString(C.PLAYER_FRACTION, "0");

        // Get the value of mNotificationAdded from SharedPreferences. Set to false as a default.
        mNotificationAdded = mSharedPreferences.getBoolean(C.NOTIFICATION_ADDED_KEY, false);

        // pokud byl alarm zaply, zapne ho i po restartu zarizeni
        if ((Intent.ACTION_BOOT_COMPLETED).equals(intent.getAction()) && mNotificationAdded) {
            setAlarms(context);

            System.out.println("onReceive přijat - po bootu nastavuji znovu alarmy");
        }
        else {

            notification = new Notification(context);

            System.out.println("onReceive přijat");
            if (wifiManager.isWifiEnabled()) {

                wifiManager.startScan();
                List<ScanResult> results = wifiManager.getScanResults();
                for (ScanResult result : results) {
                    System.out.println("Access Point MacAddr: " + result.BSSID);
                    if (C.MAC_EDUROAM.contains(result.BSSID)) {
                        System.out.println("Nasel jsem spravnou mac");
                        // posle notifikace stejne i pro geofencing
                        notification.notifikuj();
                        break;
                    } else if (results.size() == pocitadloScanu) {
                        System.out.println("není tu macovka");
                    } else {
                        System.out.println("To není správna mac, jdu dal");
                    }
                    pocitadloScanu++;
                }
            } else {
                System.out.println("není zaplá wifi, tezko to zjistim");
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
