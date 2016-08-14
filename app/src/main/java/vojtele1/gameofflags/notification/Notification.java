package vojtele1.gameofflags.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import vojtele1.gameofflags.Act1Login;
import vojtele1.gameofflags.utils.CustomRequest;
import vojtele1.gameofflags.R;
import vojtele1.gameofflags.utils.C;

/**
 * Trida zajistuje zjisteni score obou frakci a nasledne poslani/neposlani notifikace. Hracovu
 * frakci bere ze sharedPreferences.
 * Created by Leon on 28.05.2016.
 */
public class Notification {

    RequestQueue requestQueue;
    int fraction1Score, fraction2Score, counterError;
    boolean knowScoreF1, knowScoreF2;
    boolean knowResponseF1, knowResponseF2;
    String message;
    Context context;
    /**
     * Umozni nacitat a ukladat hodnoty do pameti
     */
    private SharedPreferences sharedPreferences;
    /**
     * V jake frakci byl hrac naposled
     */
    private String playerFraction;

    public Notification(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
        // Retrieve an instance of the SharedPreferences object.
        sharedPreferences = context.getSharedPreferences(C.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);

        // Get the value of playerFraction from SharedPreferences. Set to 0 (chyba) as a default.
        playerFraction = sharedPreferences.getString(C.PLAYER_FRACTION, "0");
    }
    public void sendNotification() {
        getFractionScore(knowScoreF1, knowScoreF2);
        getInfo();
    }
    private void getInfo() {
        // Looper.getMainLooper() je potreba kvuli service(geofencing), protoze service neni UI
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (knowResponseF1 && knowResponseF2) {
                    Log.e(C.LOG_NOTIFICATION, "Pocet chyb pri stahovani: " + counterError);
                    if (counterError >= 200) {
                        Log.e(C.LOG_NOTIFICATION, "odpoved nedorazila");
                    } else if (!knowScoreF1 || !knowScoreF2) {
                        getFractionScore(knowScoreF1, knowScoreF2);
                        getInfo();
                    }  else { // vim score, tvorim zpravu pro notifikaci
                        int playerFractionScore, opositeFractionScore;
                        switch (playerFraction) {
                            case "0":
                                Log.e(C.LOG_NOTIFICATION, "neznam hracovu frakci");
                                return; // neni potreba pokracovat

                            case "1":
                                playerFractionScore = fraction1Score;
                                opositeFractionScore = fraction2Score;
                                break;
                            default:  // takze je "2"
                                playerFractionScore = fraction2Score;
                                opositeFractionScore = fraction1Score;
                                break;
                        }

                        if (playerFractionScore > opositeFractionScore + 1) {// tedy vedou minimalne o 2 body
                            Log.i(C.LOG_NOTIFICATION, "nenotifikuji, hracova frakce vede dostatecne.");
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
                        Log.i(C.LOG_NOTIFICATION, "notifikuji");
                        createNotification(context, "Game of Flags", message, "Game of Flags tě potřebuje!");
                    }
                } else {
                    getInfo();
                }
            }
        }, 1000);
    }
    private void getFractionScore(boolean f1, boolean f2) {
        if (!f1) {
            knowResponseF1 = false;
            Map<String, String> params = new HashMap<>();
            params.put("ID_fraction", "1");
            CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, C.WEBVIEW_SCORE_FRACTION, params,
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

            requestQueue.add(jsObjRequest);
        }
        if (!f2) {
            knowResponseF2 = false;
            Map<String, String> params2 = new HashMap<>();
            params2.put("ID_fraction", "2");
            CustomRequest jsObjRequest2 = new CustomRequest(Request.Method.POST, C.WEBVIEW_SCORE_FRACTION, params2,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(C.LOG_NOTIFICATION, response.toString());
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
                    Log.e(C.LOG_NOTIFICATION, "" + error.getMessage());
                    knowResponseF2 = true;
                    counterError++;
                }
            });
            requestQueue.add(jsObjRequest2);
        }
    }
    private void createNotification(Context context, String msg, String msgText, String msgAlert) {

        PendingIntent notificationIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, Act1Login.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new
                NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.app_logo)
                // ta ikona, ktera je stale videt
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.app_logo))
                .setContentTitle(msg)
                .setContentText(msgText)
                .setTicker(msgAlert);

        // umozni viceradkove notifikace
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(msg);
        bigTextStyle.bigText(msgText);

        builder.setStyle(bigTextStyle);

        builder.setContentIntent(notificationIntent);

        builder.setDefaults(NotificationCompat.DEFAULT_SOUND);

        builder.setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, builder.build());

    }
}
