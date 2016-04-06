package vojtele1.gameofflags;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import vojtele1.gameofflags.dataLayer.BleScan;
import vojtele1.gameofflags.dataLayer.CellScan;
import vojtele1.gameofflags.dataLayer.Fingerprint;
import vojtele1.gameofflags.dataLayer.WifiScan;
import vojtele1.gameofflags.database.Scans;
import vojtele1.gameofflags.utils.C;
import vojtele1.gameofflags.utils.scanners.DeviceInformation;
import vojtele1.gameofflags.utils.scanners.ScanResultListener;
import vojtele1.gameofflags.utils.scanners.Scanner;

public class Act2WebView extends AppCompatActivity {
    TextView fraction1_score, fraction2_score, player_score, player_level;
    ImageButton buttonQR, buttonSettings;
    Button buttonLayer1, buttonLayer2, buttonLayer3, buttonLayer4;
    android.webkit.WebView webView;
    RequestQueue requestQueue;
    String token;
    WifiManager wm;
    Scanner scanner;
    Scans scans;
    int fingerprint, idScan, odeslano, cas, position = -1, flag;
    Cursor scan;

    String qr = "1";


    String adresa = "http://gameofflags-vojtele1.rhcloud.com/android/";

    String mapa = "http://gameofflags-vojtele1.rhcloud.com/images/j1np.png";

    String webViewPlayer = adresa + "webviewplayer";
    String webViewScoreFraction = adresa + "webviewscorefraction";
    String sendScan = adresa + "sendscan";
    String changePlayerScore = adresa + "changeplayerscore";
    String changeFlagOwner = adresa + "changeflagowner";
    String getFlagWhen = adresa + "getflagwhen";


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

        // Initiate wifi service manager
        wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        scanner = new Scanner(this);
        scans = new Scans(this);
        scan = scans.getScans();

        idScan = scan.getColumnIndex("_id");
        fingerprint = scan.getColumnIndex("fingerprint");
        odeslano = scan.getColumnIndex("odeslano");
        cas = scan.getColumnIndex("date");
        flag = scan.getColumnIndex("flag");
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
                String contents = intent.getStringExtra("SCAN_RESULT");
               // String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

              //  Toast.makeText(Act2WebView.this, "Content: " + contents + "\n" + " Format: " + format, Toast.LENGTH_LONG).show();
                if (contents.equals("Game of Flags - Tady je vlajka číslo 1.") ||
                        contents.equals("Game of Flags - Tady je vlajka číslo 2.") ||
                       // contents.equals("Game of Flags - Tady je vlajka číslo 3.") ||
                        contents.equals("Game of Flags - Tady je vlajka číslo 4.")) {

                    // TODO toto je urceni id vlajky pro databazi
                    if (contents.equals("Game of Flags - Tady je vlajka číslo 1.")) flag = 1;
                    if (contents.equals("Game of Flags - Tady je vlajka číslo 2.")) flag = 2;
                    if (contents.equals("Game of Flags - Tady je vlajka číslo 4.")) flag = 3;

                    ziskVlajkyKdy();


                } else {
                    new AlertDialog.Builder(Act2WebView.this)
                            .setTitle("Nepodváděj!")
                            .setMessage("Toto není správný qr kód.")
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            }
        }
    }
    public void writePoint(List<WifiScan> wifiScans, List<BleScan> bleScans, List<CellScan> cellScans, int flag) {
        Fingerprint p = new Fingerprint();
        p.setWifiScans(wifiScans);
        p.setBleScans(bleScans); // naplnime daty z Bluetooth
        p.setCellScans(cellScans);
        new DeviceInformation(this).fillPosition(p); // naplnime infomacemi o zarizeni
        scans.insertScan(p.toString(), flag);
    }
    public void poslaniScanu() {

        Map<String, String> params = new HashMap();
        params.put("token", token);
        params.put("flag", String.valueOf(flag));
        params.put("fingerprint", scan.getString(fingerprint));
        params.put("scanWhen", scan.getString(cas));

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST,  sendScan, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());

                        try {
                            JSONArray scansJson = response.getJSONArray("scan");
                            JSONObject scanJson = scansJson.getJSONObject(0);
                            if (scanJson.getString("scanWhen") != null) {
                                scans.updateScan(scanJson.getString("scanWhen"));
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
    }
    public void poslaniScanuVse() {
        scan = scans.getScans();
        String text_cely = "Posílání: \n";
        String text;
        if (scan.getCount() < 1) {
            System.out.println("Není tu co poslat.");
        } else {
            while (scan.moveToNext()) {
                if (scan.getString(odeslano).equals("1")) {
                    scans.deleteScan(scan.getLong(idScan));
                    position = position - 1;

                    //System.out.println("Scan je už poslán: " + scan.getString(0));
                    text = "Mažu id: " + scan.getString(0) + "\n";
                    text_cely = text_cely.concat(text);
                } else {
                    poslaniScanu();
                  //  System.out.println("Posílám id: " + scan.getString(0));
                    text = "Posílám id: " + scan.getString(0) + "\n";
                    text_cely = text_cely.concat(text);
                }
            }
            System.out.println(text_cely);
        }
    }
    private void zmenaScore() {
        Map<String, String> params = new HashMap();
        params.put("token", token);

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST,  changePlayerScore, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("zmena score: " + response.toString());

                        try {
                            JSONArray playersJson = response.getJSONArray("player");
                            JSONObject playerJson = playersJson.getJSONObject(0);
                            if (playerJson.getString("score") != null) {

                                vytahniData();

                                new AlertDialog.Builder(Act2WebView.this)
                                        .setTitle("")
                                        .setMessage("Vlajka byla zabrána!")
                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .show();
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
    }

    private void zmenaVlastnikaVlajky() {
        Map<String, String> params = new HashMap();
        params.put("token", token);
        params.put("flag", String.valueOf(flag));

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST,  changeFlagOwner, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("zmena vlastnika vlajky: " + response.toString());

                        try {
                            JSONArray flagsJson = response.getJSONArray("flag");
                            JSONObject flagJson = flagsJson.getJSONObject(0);
                            if (flagJson.getString("ID_flag") != null) {
                                zmenaScore();
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
    }
    private void ziskVlajkyKdy() {
        Map<String, String> params = new HashMap();
        params.put("token", token);
        params.put("flag", String.valueOf(flag));

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST,  getFlagWhen, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("ziskani vlajky kdy a ja naposled?: " + response.toString());

                        try {
                            JSONArray flagsJson = response.getJSONArray("flag");
                            JSONObject flagJson = flagsJson.getJSONObject(0);
                            JSONObject time = flagJson.getJSONObject("flagWhen");
                            String flagWhen = time.getString("date");
                            String flagMe = flagJson.getString("flagMe");
                            String fractionMe = flagJson.getString("fractionMe");

                            //zmena formatu casu
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                Date date = sdf.parse(flagWhen);
                                // zmeni cas podle timezony na aktualni, 18000000 je 5 hodin (posun openshiftu od UTC)
                                date.setTime(date.getTime() + TimeZone.getDefault().getRawOffset() + 18000000);
                                long dateFlagChange = date.getTime();
                                // ziskani aktualniho casu
                                Long dateNow = new Date().getTime();
                                SimpleDateFormat sdf2 = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss");

                                // pokud se vlajka menila pred mene jak 10 minutami, tak ji nelze zmenit
                                if (flagMe.equals("true")) {
                                    new AlertDialog.Builder(Act2WebView.this)
                                            .setTitle("Vlajku nemůžeš změnit!")
                                            .setMessage("Tuto vlajku jsi již zabral.")
                                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .show();
                                } else if (fractionMe.equals("true")) {
                                    new AlertDialog.Builder(Act2WebView.this)
                                            .setTitle("Vlajku nemůžeš změnit!")
                                            .setMessage("Tuto vlajku již tvoje frakce vlastní.")
                                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .show();
                                } else if (dateNow < dateFlagChange+600000) {
                                    new AlertDialog.Builder(Act2WebView.this)
                                            .setTitle("Vlajku ještě nelze změnit!")
                                            .setMessage("Změna možná: "+ sdf2.format(dateFlagChange+600000).toString())
                                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .show();
                                } else {
                                    scanner.startScan(C.SCAN_COLLECTOR_TIME, new ScanResultListener() {
                                        @Override
                                        public void onScanFinished(final List<WifiScan> wifiScans, final List<BleScan> bleScans, final List<CellScan> cellScans) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Log.d("Act2WebView", "Received onScanfinish, wifi = " + wifiScans.size() + ", ble = " + bleScans.size() + ", gsm = " + cellScans.size());
                                                    writePoint(wifiScans, bleScans, cellScans, flag);

                                                    // posle vsechny scany, i ty, ktere se drive neposlaly
                                                    poslaniScanuVse();
                                                    zmenaVlastnikaVlajky();
                                                }
                                            });
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
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
    }
}
