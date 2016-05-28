package vojtele1.gameofflags.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vojtele1.gameofflags.Act1Login;
import vojtele1.gameofflags.CustomRequest;
import vojtele1.gameofflags.R;
import vojtele1.gameofflags.utils.C;

/**
 * Created by Leon on 14.05.2016.
 */
public class AlarmReceiver extends BroadcastReceiver {

    RequestQueue requestQueue;
    String adresa = "http://gameofflags-vojtele1.rhcloud.com/android/";
    String webViewScoreFraction = adresa + "webviewscorefraction";

    int fraction1Score, fraction2Score, counterError;
    boolean knowScoreF1, knowScoreF2;
    boolean knowResponseF1, knowResponseF2;

    String message;
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

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);

        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // Retrieve an instance of the SharedPreferences object.
        mSharedPreferences = context.getSharedPreferences(C.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);

        // Get the value of mPlayerFraction from SharedPreferences. Set to 0 (chyba) as a default.
        mPlayerFraction = mSharedPreferences.getString(C.PLAYER_FRACTION, "0");

        notification = new Notification(context);


        System.out.println("onReceive přijat");
        if (wifiManager.isWifiEnabled()) {

            wifiManager.startScan();
            List<ScanResult> results = wifiManager.getScanResults();
            for (ScanResult result : results) {
                System.out.println("Access Point MacAddr: " + result.BSSID);
                //if (result.BSSID.equals("c8:3a:35:11:c6:70")) {
                if (C.MAC_EDUROAM.contains(result.BSSID)) {
                    System.out.println("Nasel jsem spravnou mac");
                    // posle notifikace stejne i pro geofencing
                    notification.notifikuj();
                    break;
                } else if (results.size() == pocitadloScanu){
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
    private void zjisti() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (knowResponseF1 && knowResponseF2) {

                    System.out.println("Pocet chyb: " + counterError);
                    if (counterError >= 200) {
                        System.out.println("konec, odpoved bohuzel nedosla");
                    } else if (!knowScoreF1 || !knowScoreF2) {
                        getFractionScore(knowScoreF1, knowScoreF2);
                        zjisti();
                    }  else { // vim score, tvorim zpravu pro notifikaci
                        int playerFractionScore, opositeFractionScore;
                        if (mPlayerFraction.equals("0")) {
                            System.out.println("Alarm notifikace - neznam hracovu frakci.");
                            return; // neni potreba pokracovat
                        } else if (mPlayerFraction.equals("1")) {
                            playerFractionScore = fraction1Score;
                            opositeFractionScore = fraction2Score;
                        } else { // takze je "2"
                            playerFractionScore = fraction2Score;
                            opositeFractionScore = fraction1Score;
                        }

                        if (playerFractionScore > opositeFractionScore + 1) {// tedy vedou minimalne o 2 body
                            System.out.println("alarm notifikace - nenotifikuji, hracova frakce vede dostatecne.");
                            return; // neni potreba pokracovat
                        } else if (playerFractionScore > opositeFractionScore) { // vedou pouze o bod
                            message = "Tvá frakce nepatrně vede, pomož jí získat větší náskok!";
                        } else if (playerFractionScore == opositeFractionScore) {
                            message = "Frakce jsou vyrovnané! Pomož své frakci získat nadvládu!";
                        } else if (playerFractionScore + 1 < opositeFractionScore) { // tedy prohrava minimalne o 2 body
                            message = "Tvá frakce prohrává, pomož jí dohnat ztrátu!";
                        } else { // prohrava pouze o bod
                            message = "Tvá frakce nepatrně prohrává, pomož jí dohnat ztrátu!";
                        }


                        System.out.println("vim vse, notifikuji");
                        createNotification(context, "Game of Flags", message, "Game of Flags tě potřebuje!");
                    }
                    } else {
                    zjisti();
                }
            }
        }, 1000);
    }
    private void createNotification(Context context, String msg, String msgText, String msgAlert) {

        PendingIntent notificationIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, Act1Login.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new
                NotificationCompat.Builder(context)
                .setContentTitle(msg)
                .setContentText(msgText)
                .setTicker(msgAlert)
                .setSmallIcon(R.drawable.ic_launcher);

        // umozni viceradkove notifikace
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(msg);
        bigTextStyle.bigText(msgText);

        mBuilder.setStyle(bigTextStyle);

        mBuilder.setContentIntent(notificationIntent);

        mBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);

        mBuilder.setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, mBuilder.build());

    }
    private void getFractionScore(boolean f1, boolean f2) {
        if (!f1) {
            knowResponseF1 = false;
            Map<String, String> params2 = new HashMap();
            params2.put("ID_fraction", "1");
            CustomRequest jsObjRequest2 = new CustomRequest(Request.Method.POST, webViewScoreFraction, params2,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println(response.toString());
                            knowResponseF1 = true;

                            try {
                                JSONArray fractions = response.getJSONArray("fraction");
                                JSONObject fraction = fractions.getJSONObject(0);
                                fraction1Score = fraction.getInt("score");
                                knowScoreF1 = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println(error.getMessage());
                    knowResponseF1 = true;
                    counterError++;
                }
            });

            requestQueue.add(jsObjRequest2);
        }
        if (!f2) {
            knowResponseF2 = false;
            Map<String, String> params3 = new HashMap();
            params3.put("ID_fraction", "2");
            CustomRequest jsObjRequest3 = new CustomRequest(Request.Method.POST, webViewScoreFraction, params3,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println(response.toString());
                            knowResponseF2 = true;

                            try {
                                JSONArray fractions = response.getJSONArray("fraction");
                                JSONObject fraction = fractions.getJSONObject(0);
                                fraction2Score = fraction.getInt("score");
                                knowScoreF2 = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println(error.getMessage());
                    knowResponseF2 = true;
                    counterError++;
                }
            });
            requestQueue.add(jsObjRequest3);
        }
    }
}