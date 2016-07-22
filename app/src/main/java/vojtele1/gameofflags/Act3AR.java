package vojtele1.gameofflags;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vojtele1.gameofflags.dataLayer.BleScan;
import vojtele1.gameofflags.dataLayer.CellScan;
import vojtele1.gameofflags.dataLayer.Fingerprint;
import vojtele1.gameofflags.dataLayer.WifiScan;
import vojtele1.gameofflags.database.Scans;
import vojtele1.gameofflags.utils.BaseActivity;
import vojtele1.gameofflags.utils.C;
import vojtele1.gameofflags.utils.CameraSource;
import vojtele1.gameofflags.utils.CustomDialog;
import vojtele1.gameofflags.utils.CustomRequest;
import vojtele1.gameofflags.utils.RetryingSender;
import vojtele1.gameofflags.utils.crashReport.ExceptionHandler;
import vojtele1.gameofflags.utils.scanners.DeviceInformation;
import vojtele1.gameofflags.utils.scanners.ScanResultListener;
import vojtele1.gameofflags.utils.scanners.Scanner;

/**
 * predelane http://code.tutsplus.com/tutorials/reading-qr-codes-using-the-mobile-vision-api--cms-24680
 */
public class Act3AR extends BaseActivity {

    SurfaceView cameraView;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    int notVisibleSecond;
    boolean knowFlagInfo = true;
    boolean alreadyVisibleQR;

    String token, flagId;
    Scanner scanner;
    Scans scans;
    int fingerprint, idScan, odeslano, cas, flagDB;
    Cursor scan;

    boolean wasBTEnabled, wasWifiEnabled;
    WifiManager wm;
    BluetoothAdapter bluetoothAdapter;

    ArrayList<String> qrCodes;


    String adresa = "http://gameofflags-vojtele1.rhcloud.com/android/";
    String sendScan = adresa + "sendscan";
    String changePlayerScore = adresa + "changeplayerscore";
    String changeFlagOwner = adresa + "changeflagowner";
    String getFlagInfoUser = adresa + "getflaginfouser";
    String getQrCodes = adresa + "getqrcodes";

    RequestQueue requestQueue;

    /**
     * Umozni nacitat a ukladat hodnoty do pameti
     */
    private SharedPreferences sharedPreferences;


    boolean flash;
    CameraSource.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        scanner = new Scanner(this);
        scans = new Scans(this);
        scan = scans.getScans();

        idScan = scan.getColumnIndex("_id");
        fingerprint = scan.getColumnIndex("fingerprint");
        odeslano = scan.getColumnIndex("odeslano");
        cas = scan.getColumnIndex("date");
        flagDB = scan.getColumnIndex("flag");

        // ziska qr cody z db a ulozi do arraylistu
        getQrCodes();

        // Retrieve an instance of the SharedPreferences object.
        sharedPreferences = getSharedPreferences(C.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);
        // Get the value of token from SharedPreferences. Set to "" as a default.
        token = sharedPreferences.getString(C.TOKEN, "");

        if (token.equals(""))
            startActivity(new Intent(this, Act1Login.class));

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        initCamera();
    }

    private void initCamera() {
        setContentView(R.layout.activity_ar);
        cameraView = (SurfaceView) findViewById(R.id.camera_view);
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024);

        builder.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        if (flash) {
            builder.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        }
        cameraSource = builder.build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException | SecurityException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
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
        barcodeDetector.setProcessor(barcodeProcessor());
    }
    private Detector.Processor<Barcode> barcodeProcessor() {
        return new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(final Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if ((CustomDialog.dialog == null || !CustomDialog.dialog.isShowing()) && !scanner.scanFinished && qrCodes != null) {

                    if (barcodes.size() != 0 && qrCodes.contains(barcodes.valueAt(0).displayValue)) {
                        // +1 kvuli poli, ktere zacina od 0, ale id v db od 1
                        flagId = String.valueOf(qrCodes.indexOf(barcodes.valueAt(0).displayValue) + 1);
                        if (!scanner.running && knowFlagInfo) {

                            Log.d("test", "jsem tu");
                            ziskVlajkyKdy();
                            knowFlagInfo = false;
                        }

                        notVisibleSecond = 0;

                    } else {
                        if (alreadyVisibleQR && scanner.running) {
                            if (notVisibleSecond == 50) {
                                scanner.stopScan();
                                alreadyVisibleQR = false;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        CustomDialog.showDialog(Act3AR.this,"Nesmíš ztratit vlajku z dohledu!");
                                    }
                                });
                            }
                            else {
                                notVisibleSecond++;
                            }
                            if (notVisibleSecond == 40)
                                System.out.println("Nevidim Qr (max 50 ticku): " + notVisibleSecond);
                        }
                    }
                }
            }
        };
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
        finish();
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
            } else
                return wasWifiEnabled || wm.setWifiEnabled(true);
        } else {
            if (!wasBTEnabled && !wasWifiEnabled) {
                wm.setWifiEnabled(false);
                return bluetoothAdapter.disable();
            } else if (!wasBTEnabled) {
                return bluetoothAdapter.disable();
            } else
                return wasWifiEnabled || wm.setWifiEnabled(false);
        }
    }
    public void writePoint(List<WifiScan> wifiScans, List<BleScan> bleScans, List<CellScan> cellScans, int flagId, String floor, int x, int y) {
        Fingerprint p = new Fingerprint();
        p.setWifiScans(wifiScans);
        p.setBleScans(bleScans); // naplnime daty z Bluetooth
        p.setCellScans(cellScans);
        new DeviceInformation(this).fillPosition(p); // naplnime infomacemi o zarizeni
        p.setCreatedDate(dateToStringServer(new Date()));
        p.setLevel(floor);
        p.setX(x);
        p.setY(y);
        Gson gson = new Gson();
        scans.insertScan(gson.toJson(p), flagId);
    }
    public void poslaniScanu() {
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("flag", scan.getString(flagDB));
        params.put("fingerprint", scan.getString(fingerprint));
        params.put("scanWhen", scan.getString(cas));

        CustomRequest customRequest = new CustomRequest(Request.Method.POST,  sendScan, params,
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
        requestQueue.add(customRequest);
    }
    public void poslaniScanuVse() {
        scan = scans.getScans();
        String text_cely = "Posílání: \n";
        String text;
        if (scan.getCount() < 1) {
            System.out.println("Není tu co poslat.");
        } else {
            for(int i = 0;i < scan.getCount();i++) {
                scan.moveToNext();
                if (scan.getString(odeslano).equals("1")) {
                    scans.deleteScan(scan.getLong(idScan));

                    text = "Mažu id: " + scan.getString(0) + "\n";
                    text_cely = text_cely.concat(text);
                } else {
                    poslaniScanu();
                    text = "Posílám id: " + scan.getString(0) + "\n";
                    text_cely = text_cely.concat(text);
                }
            }
           /* while (scan.moveToNext()) {
                if (scan.getString(odeslano).equals("1")) {
                    scans.deleteScan(scan.getLong(idScan));

                    //System.out.println("Scan je už poslán: " + scan.getString(0));
                    text = "Mažu id: " + scan.getString(0) + "\n";
                    text_cely = text_cely.concat(text);
                } else {
                    poslaniScanu();
                    //  System.out.println("Posílám id: " + scan.getString(0));
                    text = "Posílám id: " + scan.getString(0) + "\n";
                    text_cely = text_cely.concat(text);
                }
            }*/
            System.out.println(text_cely);
        }
    }
    private void zmenaScore() {
        RetryingSender r = new RetryingSender(this) {
            public CustomRequest send() {
                knowResponse = false;
                knowAnswer = false;
        Map<String, String> params = new HashMap<>();
        params.put("token", token);

        return new CustomRequest(Request.Method.POST,  changePlayerScore, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("zmena score: " + response.toString());
                        knowResponse = true;
                        try {
                            JSONArray playersJson = response.getJSONArray("player");
                            JSONObject playerJson = playersJson.getJSONObject(0);
                            if (playerJson.getString("score") != null) {
                                CustomDialog.showDialog(Act3AR.this,"Vlajka byla zabrána!", new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        // ukončí aktivitu
                                        finish();
                                    }
                                });
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
r.startSender();
    }

    private void zmenaVlastnikaVlajky() {
        RetryingSender r = new RetryingSender(this) {
            public CustomRequest send() {
                knowResponse = false;
                knowAnswer = false;
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("ID_flag", flagId);

        return new CustomRequest(Request.Method.POST,  changeFlagOwner, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("zmena vlastnika vlajky: " + response.toString());
                        knowResponse = true;

                        try {
                            JSONArray flagsJson = response.getJSONArray("flag");
                            JSONObject flagJson = flagsJson.getJSONObject(0);
                            if (flagJson.getString("ID_flag") != null) {
                                zmenaScore();
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
r.startSender();
    }
    private void ziskVlajkyKdy() {
        RetryingSender r = new RetryingSender(this) {
            public CustomRequest send() {
                knowResponse = false;
                knowAnswer = false;
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("ID_flag", flagId);

        return new CustomRequest(Request.Method.POST, getFlagInfoUser, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("ziskani vlajky kdy a ja naposled?: " + response.toString());
                        knowResponse = true;

                        try {
                            JSONArray flagsJson = response.getJSONArray("flag");
                            JSONObject flagJson = flagsJson.getJSONObject(0);
                            JSONObject time = flagJson.getJSONObject("flagWhen");
                            String flagWhen = time.getString("date");
                            String flagMe = flagJson.getString("flagMe");
                            String fractionMe = flagJson.getString("fractionMe");
                            String fractionId = flagJson.getString("ID_fraction");
                            final String floor = flagJson.getString("floor");
                            final int x = flagJson.getInt("x");
                            final int y = flagJson.getInt("y");
                            try {
                                Date date = stringToDate(flagWhen);
                                long dateFlagChange = date.getTime();
                                // ziskani aktualniho casu
                                Long dateNow = new Date().getTime();
                                    // pokud hrac danou vlajku zabral (nezavisle na frakci), nemuze ji zabrat znovu
                                    if (flagMe.equals("true")) {
                                        CustomDialog.showDialog(Act3AR.this,"Tuto vlajku jsi již zabral.", new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialog) {
                                                knowFlagInfo = true;
                                            }
                                        });
                                    // pokud vlajku vlastni hracova frakce, tak ji nelze znova zabrat
                                    } else if (fractionMe.equals("true")) {
                                        CustomDialog.showDialog(Act3AR.this,"Tuto vlajku již tvoje frakce vlastní.", new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialog) {
                                                knowFlagInfo = true;
                                            }
                                        });
                                    // pokud se vlajka menila pred mene jak 10 minutami, tak ji nelze zmenit
                                    } else if (dateNow < dateFlagChange + C.FLAG_IMMUNE_TIME) {
                                        CustomDialog.showDialog(Act3AR.this,"Změna možná: " + objectToString(dateFlagChange + C.FLAG_IMMUNE_TIME), new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialog) {
                                                knowFlagInfo = true;
                                            }
                                        });
                                    } else {
                                        ViewGroup root = (ViewGroup) findViewById(R.id.flags);

                                        scanner.startScan(C.SCAN_COLLECTOR_TIME, new ScanResultListener() {
                                            @Override
                                            public void onScanFinished(final List<WifiScan> wifiScans, final List<BleScan> bleScans, final List<CellScan> cellScans) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Log.d("Act2WebView", "Received onScanfinish, wifi = " + wifiScans.size() + ", ble = " + bleScans.size() + ", gsm = " + cellScans.size());
                                                        writePoint(wifiScans, bleScans, cellScans, Integer.parseInt(flagId), floor, x, y);
                                                        // posle vsechny scany, i ty, ktere se drive neposlaly
                                                        poslaniScanuVse();
                                                        zmenaVlastnikaVlajky();
                                                    }
                                                });
                                            }
                                        }, fractionId.equals("1"), root); // zde se predava hracova frakce a view pro vykresleni spravne animace

                                        alreadyVisibleQR = true;
                                        knowFlagInfo = true;
                                    }
                                knowAnswer = true;
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
                knowResponse = true;
                counterError++;
            }
        });
            }
        };
        r.startSender();
    }

    private void getQrCodes() {
        RetryingSender r = new RetryingSender(this) {
            public CustomRequest send() {
                knowResponse = false;
                knowAnswer = false;
                Map<String, String> params = new HashMap<>();
                return new CustomRequest(Request.Method.POST,  getQrCodes, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());
                        knowResponse = true;
                        try {
                            qrCodes = new ArrayList<>();
                            JSONArray flagsJson = response.getJSONArray("flag");
                            for (int i = 0; i < flagsJson.length(); i++) {
                                JSONObject flagJson = flagsJson.getJSONObject(i);
                                String qrCode = flagJson.getString("qrCode");
                                qrCodes.add(qrCode);
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
        r.startSender(true);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Act2WebView.class);
        startActivity(intent);
    }

    public void useFlash(View view) {
        flash = !flash;
        // zapne znovu cameru s/bez blesku
        initCamera();
    }
}