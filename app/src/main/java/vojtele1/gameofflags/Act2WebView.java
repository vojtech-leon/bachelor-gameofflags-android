package vojtele1.gameofflags;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Act2WebView extends AppCompatActivity {
    TextView fraction1_score, fraction1_name, fraction2_score, fraction2_name, player_score, player_level;
    ImageButton buttonQR, buttonSettings;
    android.webkit.WebView webViewMap;
    RequestQueue requestQueue;
    String userId;

    String webViewPlayer = "http://192.168.1.101/gameofflags/www/android/webviewplayer";
    String webViewScoreFraction = "http://192.168.1.101/gameofflags/www/android/webviewscorefraction";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        // vytahne id z activity loginu
       userId = getIntent().getStringExtra("userId");

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        fraction1_score = (TextView) findViewById(R.id.fraction1_score);
        fraction1_name = (TextView) findViewById(R.id.fraction1_name);
        fraction2_score = (TextView) findViewById(R.id.fraction2_score);
        fraction2_name = (TextView) findViewById(R.id.fraction2_name);
        player_score = (TextView) findViewById(R.id.player_score);
        player_level = (TextView) findViewById(R.id.player_level);
        buttonQR = (ImageButton) findViewById(R.id.buttonQR);
        buttonSettings = (ImageButton) findViewById(R.id.buttonSettings);
        webViewMap = (android.webkit.WebView) findViewById(R.id.webViewMap);

        // Enable Javascript
        WebSettings webSettings = webViewMap.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webViewMap.loadUrl("http://beacon.uhk.cz/fimnav-webview/?map=j2np");
        // Force links and redirects to open in the WebView instead of in a browser
        webViewMap.setWebViewClient(new WebViewClient());
        vytahniData();
    }

    public void settingsButton(View view) {
        Intent intent = new Intent(this, Act4Settings.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    public void qrButton(View view) {
        Intent intent = new Intent(this, Act3AR.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    private void vytahniData() {
        Map<String, String> params = new HashMap();
        params.put("userId", userId);

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST,  webViewPlayer, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());

                        try {
                            JSONArray players = response.getJSONArray("player");
                            for (int i = 0; i < players.length(); i++) {
                                JSONObject player = players.getJSONObject(i);
                                player_score.setText(player.getString("score"));
                                player_level.setText(player.getString("level"));

                            }

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

        Map<String, String> params2 = new HashMap();
        params2.put("ID_fraction", "1");

        CustomRequest jsObjRequest2 = new CustomRequest(Request.Method.POST, webViewScoreFraction, params2,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());

                        try {
                            JSONArray fractions = response.getJSONArray("fraction");

                                JSONObject fraction = fractions.getJSONObject(0);
                                fraction1_score.setText(fraction.getString("score"));


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

        requestQueue.add(jsObjRequest2);

        Map<String, String> params3 = new HashMap();
        params3.put("ID_fraction", "2");

        CustomRequest jsObjRequest3 = new CustomRequest(Request.Method.POST, webViewScoreFraction, params3,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());

                        try {
                            JSONArray fractions = response.getJSONArray("fraction");
                                JSONObject fraction = fractions.getJSONObject(0);
                                fraction2_score.setText(fraction.getString("score"));


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


}
