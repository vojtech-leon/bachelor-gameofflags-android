package vojtele1.gameofflags;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import vojtele1.gameofflags.notification.AlarmReceiver;
import vojtele1.gameofflags.notification.geofence.Geofencing;
import vojtele1.gameofflags.utils.BaseActivity;
import vojtele1.gameofflags.utils.C;
import vojtele1.gameofflags.utils.CustomRequest;
import vojtele1.gameofflags.utils.M;
import vojtele1.gameofflags.utils.RetryingSender;


/**
 * Trida obsahujici nastaveni aplikace
 * Created by Leon on 21.10.2015.
 */
public class Act4Settings extends BaseActivity {

    String token;
    String playerFraction;
    String playerFractionWhen;
    TextView fraction_name, fraction_when;
    Button buttonAddRemoveNotification;
    long dateFractionChange;

    Geofencing geofencing;

    AlarmReceiver alarmReceiver;

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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // vytahne token z activity loginu
        token = getIntent().getStringExtra("token");

        fraction_name = (TextView) findViewById(R.id.fraction_name);
        fraction_when = (TextView) findViewById(R.id.fraction_when);
        buttonAddRemoveNotification = (Button) findViewById(R.id.add_remove_notification_button);

        getPlayerFraction();

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
        if (M.isLocationEnabled(this)) {
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
    private void getPlayerFraction() {
        RetryingSender r = new RetryingSender(this) {
            public CustomRequest send() {
                knowResponse = false;
                knowAnswer = false;
                // zjisti frakci a cas posledni zmeny
                Map<String, String> params = new HashMap<>();
                params.put("token", token);
                return

                 new CustomRequest(Request.Method.POST,  getPlayerFraction, params,
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
                                        fraction_when.setText(R.string.never);
                                        knowAnswer = true;
                                    } else {
                                        try {
                                            Date date = stringToDate(playerFractionWhen);
                                            // ulozi prijaty cas
                                            dateFractionChange = date.getTime();
                                            // preformatuje prijaty cas do bezneho ciselneho tvaru
                                            String cas = dateToString(date);
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
            }
        };
        r.startSender(true);
    }
    private void changePlayerFraction(final String newPlayerFraction) {
        RetryingSender r = new RetryingSender(this) {
            public CustomRequest send() {
                knowResponse = false;
                knowAnswer = false;
                // zjisti frakci a cas posledni zmeny
                Map<String, String> params = new HashMap<>();
                params.put("token", token);
                params.put("ID_fraction", newPlayerFraction);
                return new CustomRequest(Request.Method.POST, changeFraction, params,
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
                                                    getPlayerFraction();
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
            }
        };
        r.startSender();
    }

    public void changeFraction(View view) {
        // ziskani aktualniho casu
        Long dateNow = new Date().getTime();
        // pokud se frakce menila pred mene jak tydnem, tak ji nelze zmenit
        if (dateNow < dateFractionChange+7*86400000) {
            showInfoDialog("Frakci nelze změnit!","Změna možná: "+ objectToString(dateFractionChange+7*86400000));
        }
        else {
            // informuje hrace o zmene frakce
            new AlertDialog.Builder(Act4Settings.this)
                    .setTitle("Změna frakce")
                    .setMessage("Opravdu chcete změnit frakci?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            // pokud je id frakce 1, zmeni ho na 2 a naopak
                            if (playerFraction.equals("1")) {
                                playerFraction = "2";
                            } else {
                                playerFraction = "1";
                            }

                            changePlayerFraction(playerFraction);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // pokud nechce zmenit frakci, tak nic nedelat
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

    }
    @Override
    public void onBackPressed() {
        // zakomentovani zabrani reakci na stisk hw back
        //super.onBackPressed();
    }
}
