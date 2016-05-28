package vojtele1.gameofflags;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

/**
 * predelane http://code.tutsplus.com/tutorials/reading-qr-codes-using-the-mobile-vision-api--cms-24680
 */
public class Act3AR extends AppCompatActivity {

    SurfaceView cameraView;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    int notVisibleSecond;
    boolean knowFlagInfo = true, scanFinished;
    boolean alreadyVisibleQR;

    RequestQueue requestQueue;
    String token, flagId;
    Scanner scanner;
    Scans scans;
    int fingerprint, idScan, odeslano, cas, position = -1, flagDB;
    Cursor scan;
    AlertDialog alertDialog;

    boolean wasBTEnabled, wasWifiEnabled;
    WifiManager wm;
    BluetoothAdapter bluetoothAdapter;


    String adresa = "http://gameofflags-vojtele1.rhcloud.com/android/";
    String sendScan = adresa + "sendscan";
    String changePlayerScore = adresa + "changeplayerscore";
    String changeFlagOwner = adresa + "changeflagowner";
    String getFlagInfoUser = adresa + "getflaginfouser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        // vytahne token z activity loginu
        token = getIntent().getStringExtra("token");

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        scanner = new Scanner(this);
        scans = new Scans(this);
        scan = scans.getScans();

        idScan = scan.getColumnIndex("_id");
        fingerprint = scan.getColumnIndex("fingerprint");
        odeslano = scan.getColumnIndex("odeslano");
        cas = scan.getColumnIndex("date");
        flagDB = scan.getColumnIndex("flag");


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        cameraView = (SurfaceView) findViewById(R.id.camera_view);
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024);

        // make sure that auto focus is an available option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = builder.setAutoFocusEnabled(true);
        }
        cameraSource = builder.build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
                } catch (SecurityException se) {
                    Log.e("CAMERA SOURCE", se.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (alertDialog == null && !scanFinished) {
                    if (barcodes.size() != 0 && C.QR_CODES.contains(barcodes.valueAt(0).displayValue)) {
                        System.out.println(barcodes.valueAt(0).displayValue);
                        System.out.println(barcodes.valueAt(0).format);
                        System.out.println(barcodes.size());
                        // +1 kvuli poli, ktere zacina od 0, ale id v db od 1
                        flagId = String.valueOf(C.QR_CODES.indexOf(barcodes.valueAt(0).displayValue) + 1);
                        if (!scanner.running && scanner.alertDialog == null && knowFlagInfo) {
                            ziskVlajkyKdy();
                            knowFlagInfo = false;
                        }

                        notVisibleSecond = 0;

                    } else {
                        if (alreadyVisibleQR) {
                            if (notVisibleSecond == 3) {
                                scanner.stopScan();
                                alreadyVisibleQR = false;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        alertDialog = new AlertDialog.Builder(Act3AR.this)
                                                .setTitle("")
                                                .setMessage("Nesmíš ztratit vlajku z dohledu!")
                                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                        alertDialog = null;
                                                    }
                                                })
                                                .show();
                                    }
                                });
                            }
                            else {
                                notVisibleSecond++;
                            }
                            System.out.println("Nevidim Qr (max 3s): " + notVisibleSecond);
                        }
                    }
                }
                // detekuje kazdou vterinu -> snizeni zateze
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        wasBTEnabled = bluetoothAdapter.isEnabled();
        wasWifiEnabled = wm.isWifiEnabled();
        changeBTWifiState(true);
        System.out.println("onResume");
    }
    @Override
    protected void onPause() {
        super.onPause();
        changeBTWifiState(false);
        System.out.println("onPause");
    }

    /**
     * Zapne BT a Wifi pokud je aktivita aktivni. Pokud bylo BT nebo Wifi zaple, zustane zaple.
     * @param enable jestli se ma bt a wifi zapnout/vypnout
     * @return true
     */
    public boolean changeBTWifiState(boolean enable) {
        if (enable) {
            if (!wasBTEnabled && !wasWifiEnabled) {
                wm.setWifiEnabled(true);
                return bluetoothAdapter.enable();
            } else if (!wasBTEnabled) {
                return bluetoothAdapter.enable();
            } else if (!wasWifiEnabled) {
                return wm.setWifiEnabled(true);
            } else {
                return true;
            }
        } else {
            if (!wasBTEnabled && !wasWifiEnabled) {
                wm.setWifiEnabled(false);
                return bluetoothAdapter.disable();
            } else if (!wasBTEnabled) {
                return bluetoothAdapter.disable();
            } else if (!wasWifiEnabled) {
                return wm.setWifiEnabled(false);
            } else {
                return true;
            }
        }
    }
    public void writePoint(List<WifiScan> wifiScans, List<BleScan> bleScans, List<CellScan> cellScans, int flagId) {
        Fingerprint p = new Fingerprint();
        p.setWifiScans(wifiScans);
        p.setBleScans(bleScans); // naplnime daty z Bluetooth
        p.setCellScans(cellScans);
        new DeviceInformation(this).fillPosition(p); // naplnime infomacemi o zarizeni
        scans.insertScan(p.toString(), flagId);
    }
    public void poslaniScanu() {

        Map<String, String> params = new HashMap();
        params.put("token", token);
        params.put("flag", scan.getString(flagDB));
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

                                alertDialog = new AlertDialog.Builder(Act3AR.this)
                                        .setTitle("")
                                        .setMessage("Vlajka byla zabrána!")
                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                alertDialog = null;
                                                // ukončí aktivitu a vrátí výsledek
                                                Intent intent = new Intent();
                                                setResult(Activity.RESULT_OK, intent);
                                                finish();
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
        params.put("ID_flag", flagId);

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
        params.put("ID_flag", flagId);

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, getFlagInfoUser, params,
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
                            String fractionId = flagJson.getString("ID_fraction");

                            //zmena formatu casu
                            SimpleDateFormat sdfPrijaty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            SimpleDateFormat sdfVysledny = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss");
                            // nastavi prijaty cas na UTC
                            sdfPrijaty.setTimeZone(TimeZone.getTimeZone("UTC"));
                            try {
                                Date date = sdfPrijaty.parse(flagWhen);
                                long dateFlagChange = date.getTime();
                                // ziskani aktualniho casu
                                Long dateNow = new Date().getTime();
                                knowFlagInfo = true;
                                    // pokud hrac danou vlajku zabral (nezavisle na frakci), nemuze ji zabrat znovu
                                    if (flagMe.equals("true")) {
                                        alertDialog = new AlertDialog.Builder(Act3AR.this)
                                                .setTitle("Vlajku nemůžeš změnit!")
                                                .setMessage("Tuto vlajku jsi již zabral.")
                                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                        alertDialog = null;
                                                    }
                                                })
                                                .show();
                                        // pokud vlajku vlastni hracova frakce, tak ji nelze znova zabrat
                                    } else if (fractionMe.equals("true")) {
                                        alertDialog = new AlertDialog.Builder(Act3AR.this)
                                                .setTitle("Vlajku nemůžeš změnit!")
                                                .setMessage("Tuto vlajku již tvoje frakce vlastní.")
                                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                        alertDialog = null;
                                                    }
                                                })
                                                .show();
                                        // pokud se vlajka menila pred mene jak 10 minutami, tak ji nelze zmenit
                                    } else if (dateNow < dateFlagChange + C.FLAG_IMMUNE_TIME) {
                                        alertDialog = new AlertDialog.Builder(Act3AR.this)
                                                .setTitle("Vlajku ještě nelze změnit!")
                                                .setMessage("Změna možná: " + sdfVysledny.format(dateFlagChange + C.FLAG_IMMUNE_TIME))
                                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                        alertDialog = null;
                                                    }
                                                })
                                                .show();
                                    } else {
                                        alreadyVisibleQR = true;

                                        ViewGroup root = (ViewGroup) findViewById(R.id.flags);

                                        scanner.startScan(C.SCAN_COLLECTOR_TIME, new ScanResultListener() {
                                            @Override
                                            public void onScanFinished(final List<WifiScan> wifiScans, final List<BleScan> bleScans, final List<CellScan> cellScans) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Log.d("Act2WebView", "Received onScanfinish, wifi = " + wifiScans.size() + ", ble = " + bleScans.size() + ", gsm = " + cellScans.size());
                                                        writePoint(wifiScans, bleScans, cellScans, Integer.parseInt(flagId));

                                                        scanFinished = true;
                                                        // posle vsechny scany, i ty, ktere se drive neposlaly
                                                        poslaniScanuVse();
                                                        zmenaVlastnikaVlajky();
                                                    }
                                                });
                                            }
                                        }, fractionId.equals("1"), root); // zde se predava hracova frakce a view pro vykresleni spravne animace
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
                knowFlagInfo = true;
            }
        });
        requestQueue.add(jsObjRequest);
    }

}