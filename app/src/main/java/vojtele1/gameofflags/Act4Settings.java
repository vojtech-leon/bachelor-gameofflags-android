package vojtele1.gameofflags;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
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
 * Trida obsahujici nastaveni aplikace
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

    AlarmReceiver alarmReceiver;



    int counterError;
    boolean knowAnswer, knowFName;
    boolean knowResponse, knowFNameResponse;
    ProgressDialog progressDialog;

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

        mGetPlayerFraction();

        geofencing = new Geofencing(this);
        alarmReceiver = new AlarmReceiver();

        // Retrieve an instance of the SharedPreferences object.
        mSharedPreferences = getSharedPreferences(C.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);

        // Get the value of mNotificationAdded from SharedPreferences. Set to false as a default.
        mNotificationAdded = mSharedPreferences.getBoolean(C.NOTIFICATION_ADDED_KEY, false);

        if (mNotificationAdded) {
            buttonAddRemoveNotification.setText(R.string.button_notification_remove);
        }

    }
    public void logoutButton(View view) {
        Intent intent = new Intent(this, Act1Login.class);
        startActivity(intent);
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
                alarmReceiver.setAlarms(this);


            } else {

                buttonAddRemoveNotification.setText(R.string.button_notification_add);

                if (geofencing.mGeofencesAdded) {
                    geofencing.removeGeofencesButtonHandler(view);
                }
                alarmReceiver.removeAlarms(this);
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
    private void mGetPlayerFraction() {
        if (progressDialog == null || !progressDialog.isShowing()) {
            counterError = 0;
            showProgressDialogLoading();
            mDBPlayerFraction();
            System.out.println("zapinam progress v getPlayerFraction");
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (knowResponse) {
                    System.out.println("Pocet chyb: " + counterError);
                    if (counterError >= 200) {
                        System.out.println("Konec, spravna odpoved bohuzel nedosla (same errory).");
                        new AlertDialog.Builder(Act4Settings.this)
                                .setMessage("Problém s připojením k databázi, zkuste to prosím později.")
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        progressDialog.dismiss();
                                    }
                                })
                                .show();
                    } else if (!knowAnswer) {
                        mDBPlayerFraction();
                        mGetPlayerFraction();
                    } else {
                        progressDialog.dismiss();
                        System.out.println("Loading dokoncen.");
                    }
                } else {
                    mGetPlayerFraction();
                }
            }
        }, 100);
    }
    private void mDBPlayerFraction() {
        knowResponse = false;
        knowAnswer = false;
        // zjisti frakci a cas posledni zmeny
        Map<String, String> params = new HashMap();
        params.put("token", token);

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST,  getPlayerFraction, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());
                        knowResponse = true;
                        try {
                            JSONArray players = response.getJSONArray("player");

                            JSONObject player = players.getJSONObject(0);
                            playerFraction = player.getString("ID_fraction");
                            JSONObject time = player.getJSONObject("changeFractionWhen");
                            playerFractionWhen = time.getString("date");
                            // nastavi nazev frakce
                            fraction_name.setText(player.getString("fractionName"));
                            // Ulozi aktualni hracovu frakci kvuli notifikacim
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putString(C.PLAYER_FRACTION, playerFraction);
                            editor.apply();


                            if (playerFractionWhen.equals("-0001-11-30 00:00:00.000000")) {
                                fraction_when.setText("Nikdy.");
                            } else {
                                //zmena formatu casu
                                SimpleDateFormat sdfPrijaty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                SimpleDateFormat sdfVysledny = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss");
                                // nastavi prijaty cas na UTC
                                sdfPrijaty.setTimeZone(TimeZone.getTimeZone("UTC"));
                                try {
                                    Date date = sdfPrijaty.parse(playerFractionWhen);
                                    // ulozi prijaty cas v timestampu
                                    dateFractionChange = date.getTime();
                                    // preformatuje prijaty cas do bezneho ciselneho tvaru
                                    String cas = sdfVysledny.format(dateFractionChange);
                                    // vypise cas do textView
                                    fraction_when.setText(cas);

                                    knowAnswer = true;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.append(error.getMessage());
                knowResponse = true;
                counterError++;
            }
        });

        requestQueue.add(jsObjRequest);
    }
    private void mChangeFraction(final String newPlayerFraction) {
        if (progressDialog == null || !progressDialog.isShowing()) {
            counterError = 0;
            showProgressDialogLoading();
            mDBChangeFraction(newPlayerFraction);
            System.out.println("zapinam progress v mChangeFraction");
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (knowResponse) {
                    System.out.println("Pocet chyb: " + counterError);
                    if (counterError >= 200) {
                        System.out.println("Konec, spravna odpoved bohuzel nedosla (same errory).");
                        new AlertDialog.Builder(Act4Settings.this)
                                .setMessage("Problém s připojením k databázi, zkuste to prosím později.")
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        progressDialog.dismiss();
                                    }
                                })
                                .show();
                    } else if (!knowAnswer) {
                        mDBChangeFraction(newPlayerFraction);
                        mChangeFraction(newPlayerFraction);
                    } else {
                        progressDialog.dismiss();
                        System.out.println("Loading dokoncen.");
                    }
                } else {
                    mChangeFraction(newPlayerFraction);
                }
            }
        }, 100);
    }
    private void mDBChangeFraction(String newPlayerFraction) {
        knowResponse = false;
        knowAnswer = false;
        Map<String, String> params = new HashMap();
        params.put("token", token);
        params.put("ID_fraction", newPlayerFraction);

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, changeFraction, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());
                        knowResponse = true;
                        String answer = "";
                        try {
                            JSONArray players = response.getJSONArray("fraction");
                            JSONObject player = players.getJSONObject(0);
                            answer = player.getString("zmeneno");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (answer.equals("ano")) {
                            knowAnswer = true;
                             //zobrazi zpravu o uspesne zmene frakce
                            new AlertDialog.Builder(Act4Settings.this)
                                    .setMessage("Frakce byla změněna.")
                                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            // obnovi data v textview
                                            mGetPlayerFraction();
                                        }
                                    })
                                    .show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.append(error.getMessage());
                knowResponse = true;
                counterError++;
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
                    .setMessage("Změna možná: "+ sdf.format(dateFractionChange+7*86400000))
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
                    .setTitle("Změna frakce")
                    .setMessage("Opravdu chcete změnit frakci?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            // pokud je id frakce 1, zmeni ho na 2 a naopak
                            if (playerFraction == "1") {
                                playerFraction = "2";
                            } else {
                                playerFraction = "1";
                            }

                            mChangeFraction(playerFraction);
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
        knowFNameResponse = false;
        Map<String, String> params3 = new HashMap();
        params3.put("ID_fraction", playerFraction);

        CustomRequest jsObjRequest3 = new CustomRequest(Request.Method.POST,  getFractionName, params3,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());
                        knowFNameResponse = true;
                        try {
                            JSONArray fractions = response.getJSONArray("fraction");
                            JSONObject fraction = fractions.getJSONObject(0);

                            // nastavi nazev frakce
                            fraction_name.setText(fraction.getString("name"));
                            knowFName = true;
                            //Toast.makeText(Act4Settings.this, playerFraction, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.append(error.getMessage());
                knowFNameResponse = true;
                counterError++;
            }
        });

        requestQueue.add(jsObjRequest3);
    }

    private ProgressDialog showProgressDialogLoading() {

        progressDialog = new ProgressDialog(this);

        progressDialog.show();
       // progressDialog.setCancelable(false);

        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        progressDialog.setContentView(R.layout.progress_dialog_loading);

        return progressDialog;
    }

}
