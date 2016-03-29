package vojtele1.gameofflags;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
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

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Leon on 21.10.2015.
 */
public class Act4Settings extends AppCompatActivity {

    String token;
    RequestQueue requestQueue;
    String playerFraction;
    String playerFractionWhen;
    TextView fraction_name, fraction_when;
    Long dateFractionChange = 0L;


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

        playerFraction = "3";
        vytahniData();

        System.out.println("Act2: " + token);
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
                                    date.setTime(date.getTime() + TimeZone.getDefault().getRawOffset() + 18000000);
                                    // datum pro zmenu frakce
                                    dateFractionChange = date.getTime();
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
}
