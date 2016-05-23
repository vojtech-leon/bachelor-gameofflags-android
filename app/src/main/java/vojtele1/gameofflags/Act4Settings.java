package vojtele1.gameofflags;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import vojtele1.gameofflags.notification.AlarmReceiver;
import vojtele1.gameofflags.notification.geofence.Geofencing;
import vojtele1.gameofflags.utils.C;


/**
 * Created by Leon on 21.10.2015.
 */
public class Act4Settings extends AppCompatActivity {

    String token;
    RequestQueue requestQueue;
    String playerFraction;
    String playerFractionWhen;
    TextView fraction_name, fraction_when;
    Button buttonAddRemoveNotification;
    Long dateFractionChange = 0L;

    Geofencing geofencing;


    AlarmManager alarmManager;
    Intent alarmIntent;
    PendingIntent alarmPendingIntent, alarmPendingIntent2, alarmPendingIntent3, alarmPendingIntent4;

    /**
     * Umozni nacitat a ukladat hodnoty do pameti
     */
    private SharedPreferences mSharedPreferences;

    /**
     * Jestli jsou notifikace pridany
     */
    private boolean mNotificationAdded;

    String adresa = "http://gameofflags-vojtele1.rhcloud.com/android/";

    String changeFraction = adresa + "changefraction";
    String getPlayerFraction = adresa + "getplayerfraction";
    String getFractionName = adresa + "getfractionname";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // vytahne token z activity loginu
        token = getIntent().getStringExtra("token");

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        fraction_name = (TextView) findViewById(R.id.fraction_name);
        fraction_when = (TextView) findViewById(R.id.fraction_when);
        buttonAddRemoveNotification = (Button) findViewById(R.id.add_remove_notification_button);

        playerFraction = "3";
        vytahniData();

        geofencing = new Geofencing(this);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(this, 1, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmPendingIntent2 = PendingIntent.getBroadcast(this, 2, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmPendingIntent3 = PendingIntent.getBroadcast(this, 3, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmPendingIntent4 = PendingIntent.getBroadcast(this, 4, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Retrieve an instance of the SharedPreferences object.
        mSharedPreferences = getSharedPreferences(C.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);

        // Get the value of mGeofencesAdded from SharedPreferences. Set to false as a default.
        mNotificationAdded = mSharedPreferences.getBoolean(C.NOTIFICATION_ADDED_KEY, false);

        if (mNotificationAdded) {
            buttonAddRemoveNotification.setText(R.string.button_notification_remove);
        }

    }
    public void logoutButton(View view) {
        Intent intent = new Intent(this, Act1Login.class);
        startActivity(intent);
    }
    private void vytahniData() {

        // zjisti frakci a cas posledni zmeny
        Map<String, String> params = new HashMap();
        params.put("token", token);

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST,  getPlayerFraction, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());

                        try {
                            JSONArray players = response.getJSONArray("player");

                            JSONObject player = players.getJSONObject(0);
                            playerFraction = player.getString("ID_fraction");
                            JSONObject time = player.getJSONObject("changeFractionWhen");
                            playerFractionWhen = time.getString("date");
                            if (playerFractionWhen.equals("-0001-11-30 00:00:00.000000")) {
                                fraction_when.setText("Nikdy.");
                            } else {
                                //zmena formatu casu
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                SimpleDateFormat sdf2 = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss");
                                try {
                                    Date date = sdf.parse(playerFractionWhen);
                                    // zmeni cas podle timezony na aktualni, 18000000 je 5 hodin (posun openshiftu od UTC)
                                    //date.setTime(date.getTime() + TimeZone.getDefault().getRawOffset() + 18000000);
                                    // datum pro zmenu frakce udava spravne casove pasmo
                                    dateFractionChange = date.getTime() + TimeZone.getDefault().getRawOffset();
                                    String cas = sdf2.format(date).toString();
                                    System.out.println("Date ->" + date);
                                    fraction_when.setText(cas.toString());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                                // nastavi nazev frakce
                                zmenaVypisuNazvuFrakce();
                            }catch(JSONException e){
                                e.printStackTrace();
                            }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.append(error.getMessage());

            }
        });

        requestQueue.add(jsObjRequest);
    }
    public void changeFraction(View view) {
        // ziskani aktualniho casu
        Long dateNow = new Date().getTime();
        System.out.println("DateNow -> " + dateNow);
        System.out.println("DateFractionChange -> " + dateFractionChange);

        SimpleDateFormat sdf = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss");

        // pokud se frakce menila pred mene jak tydnem, tak ji nelze zmenit
        if (dateNow < dateFractionChange+7*86400000) {
            new AlertDialog.Builder(Act4Settings.this)
                    .setTitle("Frakci nelze změnit!")
                    .setMessage("Změna možná: "+ sdf.format(dateFractionChange+7*86400000).toString())
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
        else {
            // informuje hrace o zmene frakce
            new AlertDialog.Builder(Act4Settings.this)
                    .setTitle("Změnit frakci")
                    .setMessage("Opravdu chcete změnit frakci?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            // pokud je id frakce 1, zmeni ho na 2 a naopak
                            if (playerFraction == "1") {
                                playerFraction = "2";
                            } else {
                                playerFraction = "1";
                            }

                            Map<String, String> params2 = new HashMap();
                            params2.put("token", token);
                            params2.put("ID_fraction", playerFraction);

                            CustomRequest jsObjRequest2 = new CustomRequest(Request.Method.POST, changeFraction, params2,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            System.out.println(response.toString());
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    System.out.append(error.getMessage());

                                }
                            });

                            requestQueue.add(jsObjRequest2);

                            // zpomaleni kodu kvuli nacitani aktualnich dat (obcas se nestiha nacist)
                            // 100 ms ve vetsine pripadu postaci
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // obnovi data ve vypisu
                                    vytahniData();
                                    //zobrazi zpravu o uspesne zmene frakce
                                    new AlertDialog.Builder(Act4Settings.this)
                                            .setTitle("Změna frakce")
                                            .setMessage("Frakce byla změněna.")
                                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .show();
                                }
                            }, 100);


                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // pokud nechce zmenit frakci, tak nic nedelat
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

    }

    public void zmenaVypisuNazvuFrakce() {
        Map<String, String> params3 = new HashMap();
        params3.put("ID_fraction", playerFraction);

        CustomRequest jsObjRequest3 = new CustomRequest(Request.Method.POST,  getFractionName, params3,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());

                        try {
                            JSONArray fractions = response.getJSONArray("fraction");
                            JSONObject fraction = fractions.getJSONObject(0);

                            // nastavi nazev frakce
                            fraction_name.setText(fraction.getString("name"));

                            //Toast.makeText(Act4Settings.this, playerFraction, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.append(error.getMessage());

            }
        });

        requestQueue.add(jsObjRequest3);
    }
    public void backButton(View view) {
        Intent intent = new Intent(this, Act2WebView.class);
        intent.putExtra("token", token);
        startActivity(intent);
    }

    public void addRemoveNotificationButton(View view) {
        if (C.isLocationEnabled(this)) {
            if (!mNotificationAdded) {

                buttonAddRemoveNotification.setText(R.string.button_notification_remove);

                if (!geofencing.mGeofencesAdded) {
                    geofencing.addGeofencesButtonHandler(view);
                }

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 00);
                calendar.set(Calendar.SECOND, 00);
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
            } else {

                buttonAddRemoveNotification.setText(R.string.button_notification_add);

                if (geofencing.mGeofencesAdded) {
                    geofencing.removeGeofencesButtonHandler(view);
                }
                alarmManager.cancel(alarmPendingIntent);
                alarmManager.cancel(alarmPendingIntent2);
                alarmManager.cancel(alarmPendingIntent3);
                alarmManager.cancel(alarmPendingIntent4);
            }

            // Update state and save in shared preferences.
            mNotificationAdded = !mNotificationAdded;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(C.NOTIFICATION_ADDED_KEY, mNotificationAdded);
            editor.apply();

            Toast.makeText(
                    this,
                    getString(mNotificationAdded ? R.string.notification_added :
                            R.string.notification_removed),
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    this,
                    "Zapněte prosím polohu (po nastavení můžete vypnout).",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
