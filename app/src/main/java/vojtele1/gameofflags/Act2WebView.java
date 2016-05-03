package vojtele1.gameofflags;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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


public class Act2WebView extends AppCompatActivity {
    TextView fraction1_score, fraction2_score, player_score, player_level;
    ImageButton buttonQR, buttonSettings;
    Button buttonLayer1, buttonLayer2, buttonLayer3, buttonLayer4;
    android.webkit.WebView webView;
    RequestQueue requestQueue;
    String token;

    String adresa = "http://gameofflags-vojtele1.rhcloud.com/android/";

    String mapa = "http://gameofflags-vojtele1.rhcloud.com/images/j1np.png";

    String webViewPlayer = adresa + "webviewplayer";
    String webViewScoreFraction = adresa + "webviewscorefraction";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        // vytahne token z activity loginu
        token = getIntent().getStringExtra("token");

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        fraction1_score = (TextView) findViewById(R.id.fraction1_score);
        fraction2_score = (TextView) findViewById(R.id.fraction2_score);
        player_score = (TextView) findViewById(R.id.player_score);
        player_level = (TextView) findViewById(R.id.player_level);
        buttonQR = (ImageButton) findViewById(R.id.buttonQR);
        buttonSettings = (ImageButton) findViewById(R.id.buttonSettings);
        webView = (android.webkit.WebView) findViewById(R.id.webViewMap);

        buttonLayer1 = (Button) findViewById(R.id.buttonLayer1);
        buttonLayer2 = (Button) findViewById(R.id.buttonLayer2);
        buttonLayer3 = (Button) findViewById(R.id.buttonLayer3);
        buttonLayer4 = (Button) findViewById(R.id.buttonLayer4);

        // Enable Javascript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // zmena velikosti obsahu, aby se vesel cely na sirku
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        // Zapnuti zoom controls
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);

        webView.loadUrl(mapa);

        // Force links and redirects to open in the WebView instead of in a browser
        webView.setWebViewClient(new WebViewClient());
        vytahniData();

        System.out.println("Act2: " + token);
    }

    public void settingsButton(View view) {
        Intent intent = new Intent(this, Act4Settings.class);
        intent.putExtra("token", token);
        startActivity(intent);
    }

    private void vytahniData() {
        Map<String, String> params = new HashMap();
        params.put("token", token);

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST,  webViewPlayer, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());

                        try {
                            JSONArray players = response.getJSONArray("player");

                                JSONObject player = players.getJSONObject(0);
                                player_score.setText(player.getString("score"));
                                player_level.setText(player.getString("level"));



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

    public void layer1Button(View view) {
        mapa = "http://gameofflags-vojtele1.rhcloud.com/images/j1np.png";
        webView.loadUrl(mapa);
    }
    public void layer2Button(View view) {
        mapa = "http://gameofflags-vojtele1.rhcloud.com/images/j2np.png";
        webView.loadUrl(mapa);
    }
    public void layer3Button(View view) {
        mapa = "http://gameofflags-vojtele1.rhcloud.com/images/j3np.png";
        webView.loadUrl(mapa);
    }
    public void layer4Button(View view) {
        mapa = "http://gameofflags-vojtele1.rhcloud.com/images/j4np.png";
        webView.loadUrl(mapa);
    }

    public void qrButton(View view) {
        Intent intent = new Intent(this, Act3AR.class);
        intent.putExtra("token", token);
        startActivityForResult(intent, 0);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {

                vytahniData();

            } else if (resultCode == RESULT_CANCELED) {
                // To Handle cancel
                System.out.println("Act 2 - Zabrání vlajky se nepodařilo.");
            }
        }
    }
}
