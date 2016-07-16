
package vojtele1.gameofflags;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import vojtele1.gameofflags.utils.BaseActivity;
import vojtele1.gameofflags.utils.C;
import vojtele1.gameofflags.utils.CustomRequest;
import vojtele1.gameofflags.utils.RetryingSender;
import vojtele1.gameofflags.utils.WebviewOnClick;


public class Act2WebView extends BaseActivity {
    TextView fraction1_score, fraction2_score, player_score, fraction1_name, fraction2_name;
    ImageButton buttonQR, buttonSettings;
    Button buttonLayer1, buttonLayer2, buttonLayer3, buttonLayer4;
    android.webkit.WebView webView;
    String token;
    String floor;

    String adresa = "http://gameofflags-vojtele1.rhcloud.com/android/";
    String webViewPlayer = adresa + "webviewplayer";
    String webViewScoreFraction = adresa + "webviewscorefraction";
    String getFlagInfo = adresa + "getflaginfo";

    /**
     * Umozni nacitat a ukladat hodnoty do pameti
     */
    private SharedPreferences sharedPreferences;

    /**
     * Jake patro bylo naposledy zobrazeno
     */
    private String shownFloor;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        // vytahne token z activity loginu
        token = getIntent().getStringExtra("token");

        fraction1_score = (TextView) findViewById(R.id.fraction1_score);
        fraction2_score = (TextView) findViewById(R.id.fraction2_score);
        fraction1_name = (TextView) findViewById(R.id.fraction1_name);
        fraction2_name = (TextView) findViewById(R.id.fraction2_name);
        player_score = (TextView) findViewById(R.id.player_score);
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
        webView.addJavascriptInterface(new WebviewOnClick(this), "Android");
        // zmena velikosti obsahu, aby se vesel cely na sirku
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        // Zapnuti zoom controls
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);

        // Retrieve an instance of the SharedPreferences object.
        sharedPreferences = getSharedPreferences(C.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);

        // Get the value of mNotificationAdded from SharedPreferences. Set to false as a default.
        shownFloor = sharedPreferences.getString(C.SHOWN_FLOOR, "J1NP");

        webView.loadUrl("file:///android_asset/" + shownFloor + ".html");
        floor = shownFloor;

        // Force links and redirects to open in the WebView instead of in a browser
       // webView.setWebViewClient(new WebViewClient());

        vytahniData();
    }

    public void settingsButton(View view) {
        Intent intent = new Intent(this, Act4Settings.class);
        intent.putExtra("token", token);
        startActivity(intent);
    }

    private void vytahniData() {
        RetryingSender r = new RetryingSender(this) {
            public CustomRequest send() {
                knowResponse = false;
                knowAnswer = false;
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        return new CustomRequest(Request.Method.POST,  webViewPlayer, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());
                        knowResponse = true;

                        try {
                            JSONArray players = response.getJSONArray("player");

                                JSONObject player = players.getJSONObject(0);
                                player_score.setText(player.getString("score"));
                            if (player.getInt("fraction") == 1) {
                                fraction1_name.setText("•");
                            } else {
                                fraction2_name.setText("•");
                            }
                            knowAnswer = true;
                            // musi to byt po urcite dobe, jinak se webview nestihlo nacist a body
                            // nezobrazilo - pockani na odpoved prvniho dotazu je dostacujici
                            showFlags(floor);


                        } catch (JSONException e) {
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
        r.startSender();
        RetryingSender r2 = new RetryingSender(this) {
            public CustomRequest send() {
                knowResponse = false;
                knowAnswer = false;
        Map<String, String> params2 = new HashMap<>();
        params2.put("ID_fraction", "1");
        return new CustomRequest(Request.Method.POST, webViewScoreFraction, params2,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());
                        knowResponse = true;

                        try {
                            JSONArray fractions = response.getJSONArray("fraction");

                                JSONObject fraction = fractions.getJSONObject(0);
                                fraction1_score.setText(fraction.getString("score"));


                            knowAnswer = true;
                        } catch (JSONException e) {
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
r2.startSender();
        RetryingSender r3 = new RetryingSender(this) {
public CustomRequest send() {
    knowResponse = false;
    knowAnswer = false;
        Map<String, String> params3 = new HashMap<>();
        params3.put("ID_fraction", "2");
       return new CustomRequest(Request.Method.POST, webViewScoreFraction, params3,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());
                        knowResponse = true;

                        try {
                            JSONArray fractions = response.getJSONArray("fraction");
                                JSONObject fraction = fractions.getJSONObject(0);
                                fraction2_score.setText(fraction.getString("score"));


                            knowAnswer = true;
                        } catch (JSONException e) {
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
        r3.startSender();

    }

    public void layer1Button(View view) {
        floor = "J1NP";
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(C.SHOWN_FLOOR, floor);
        editor.apply();
        webView.loadUrl("file:///android_asset/" + floor + ".html");
        vytahniData();
    }
    public void layer2Button(View view) {
        floor = "J2NP";
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(C.SHOWN_FLOOR, floor);
        editor.apply();
        webView.loadUrl("file:///android_asset/" + floor + ".html");
        vytahniData();
    }
    public void layer3Button(View view) {
        floor = "J3NP";
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(C.SHOWN_FLOOR, floor);
        editor.apply();
        webView.loadUrl("file:///android_asset/" + floor + ".html");
        vytahniData();
    }
    public void layer4Button(View view) {
        floor = "J4NP";
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(C.SHOWN_FLOOR, floor);
        editor.apply();
        webView.loadUrl("file:///android_asset/" + floor + ".html");
        vytahniData();
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

     private void createPoint(int id, int x, int y, String color) {
        webView.loadUrl("javascript:createCircle(" + String.valueOf(id) + ", " + String.valueOf(x) + ", " + String.valueOf(y) + ", \"" + color + "\")");
    }

    private void showFlags(final String floor) {
         RetryingSender r4 = new RetryingSender(this) {
            public CustomRequest send() {
                knowResponse = false;
                knowAnswer = false;
                Map<String, String> params = new HashMap<>();
                params.put("floor", floor);
                return new CustomRequest(Request.Method.POST, getFlagInfo, params,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                System.out.println(response.toString());
                                knowResponse = true;
                                try {
                                    JSONArray flagsJson = response.getJSONArray("flag");
                                    for (int i = 0; i < flagsJson.length(); i++) {
                                        JSONObject flagJson = flagsJson.getJSONObject(i);
                                        JSONObject time = flagJson.getJSONObject("flagWhen");
                                        String flagWhen = time.getString("date");
                                        int idFraction = flagJson.getInt("ID_fraction");
                                        int idFlag = flagJson.getInt("ID_flag");
                                        int x = flagJson.getInt("x");
                                        int y = flagJson.getInt("y");
                                        try {
                                            Date date = stringToDate(flagWhen);
                                            long dateFlagChange = date.getTime();
                                            // ziskani aktualniho casu
                                            Long dateNow = new Date().getTime();
                                            if (dateNow < dateFlagChange + C.FLAG_IMMUNE_TIME) {
                                                if (idFraction == 1) {
                                                    createPoint(idFlag, x, y, "#FF8080");
                                                } else {
                                                    createPoint(idFlag, x, y, "#8080FF");
                                                }
                                            } else {
                                                if (idFraction == 1) {
                                                    createPoint(idFlag, x, y, "#FF0000");
                                                } else {
                                                    createPoint(idFlag, x, y, "#0000FF");
                                                }
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    knowAnswer = true;
                                } catch (JSONException e) {
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
    r4.startSender();
    }
    @Override
    public void onBackPressed() {
        // zakomentovani zabrani reakci na stisk hw back
        //super.onBackPressed();
    }
}
