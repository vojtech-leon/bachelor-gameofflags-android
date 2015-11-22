package vojtele1.gameofflags;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Leon on 21.10.2015.
 */
public class Act4Settings extends AppCompatActivity {

    String userId;
    RequestQueue requestQueue;
    String playerFraction;
    String playerFractionWhen;
    TextView fraction_name, fraction_when;
    Button buttonChangeFraction;

    String changeFraction = "http://192.168.1.101/gameofflags/www/android/changeFraction";
    String getPlayerFraction = "http://192.168.1.101/gameofflags/www/android/getPlayerFraction";
    String getFractionName = "http://192.168.1.101/gameofflags/www/android/getFractionName";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // vytahne id z activity loginu
        userId = getIntent().getStringExtra("userId");

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        fraction_name = (TextView) findViewById(R.id.fraction_name);
        fraction_when = (TextView) findViewById(R.id.fraction_when);
        buttonChangeFraction = (Button) findViewById(R.id.buttonChangeFraction);

        vytahniData();
    }
    public void logout(View view) {
        Intent intent = new Intent(this, Act1Login.class);
        startActivity(intent);
    }
    private void vytahniData() {

        // zjisti frakci a cas posledni zmeny
        Map<String, String> params = new HashMap();
        params.put("userId", userId);

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST,  getPlayerFraction, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());

                        try {
                            JSONArray players = response.getJSONArray("player");

                                JSONObject player = players.getJSONObject(0);
                                playerFraction = player.getString("ID_fraction");
                            fraction_when.setText(player.getString("ID_fraction"));
                          //      playerFractionWhen = player.getString("changeFractionWhen");
                            Toast.makeText(Act4Settings.this, "ahoj", Toast.LENGTH_LONG).show();

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

        requestQueue.add(jsObjRequest);
/*
        // pokud nebyla menena frakce, tak napis nikdy
        if (playerFractionWhen == "0000-00-00 00:00:00") {
            fraction_when.setText("nikdy");
        } else {
            // TODO zmena vypisu casu
            fraction_when.setText(playerFractionWhen);
        }
*/
        // vytahne nazev frakce

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
    public void changeFraction() {


        //------------------------------
        if (playerFraction == "1") {
            playerFraction = "2";
        } else {
            playerFraction = "1";
        }

        //-----------------------------------


        Map<String, String> params2 = new HashMap();
        params2.put("userId", userId);
        params2.put("ID_fraction", playerFraction);

        CustomRequest jsObjRequest2 = new CustomRequest(Request.Method.POST,  changeFraction, params2,
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


    }
}
