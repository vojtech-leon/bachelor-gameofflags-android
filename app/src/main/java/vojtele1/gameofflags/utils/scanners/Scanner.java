package vojtele1.gameofflags.utils.scanners;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import vojtele1.gameofflags.dataLayer.BleScan;
import vojtele1.gameofflags.dataLayer.CellScan;
import vojtele1.gameofflags.dataLayer.WifiScan;
import vojtele1.gameofflags.utils.C;
import vojtele1.gameofflags.utils.StepDetector;


/**
 * Trida pro komplexni skenovani (BLE,WIFI,GSM) po urcitou dobu, behem skenovani zobrazuje progress dialog s pocty naskenovani. Zahajeni skenovani pomoci metod startScan(...) a preruseno pomoci stopScan().
 * Pri pouziti je nutne vzdy v onPause aktivity ukoncovat skenovani (stopScan()) aby nedochazelo k leakovani receiveru a padu aplikace
 * Created by Matej Danicek on 7.11.2015.
 */
public class Scanner {
    Context context;

    List<BleScan> bleScans = new ArrayList<>();
    List<WifiScan> wifiScans = new ArrayList<>();
    List<CellScan> cellScans = new ArrayList<>();

    long startTime;
    /**
     * zda prave probiha sken
     */
    public boolean running;
    /**
     * zda ma byt znovu spusteno cyklicke synchronni skenovani (wifi a gsm)
     */
    boolean cont = false;

    DefaultBeaconConsumer beaconConsumer;
    WifiManager wm;
    BroadcastReceiver wifiBroadcastReceiver;

    ProgressDialog progressDialog;
    Timer timer;
    CountDownTimer cdt;
    public AlertDialog alertDialog;

    StepDetector stepDetector;

    public Scanner(Context context) {
        this.context = context;
        init();
    }

    /**
     * Priprava broadcast receiveru, manazeru apod.
     */
    private void init() {
        beaconConsumer = new ImmediateBeaconConsumer(this, context, false);

        //pripravi wifiManager a broadcast receiver
        wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //receiver pro prijem naskenovanych wifi siti
        wifiBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(C.LOG_WIFISCAN, "Number of wifi networks scanned: " + wm.getScanResults().size() + ", " + (running ? "Scanning again" : "Finished scanning"));
                wifiScans.addAll(convertWifiResults(wm.getScanResults()));
                if (cont) {
                    wm.startScan();
                }
            }
        };
        stepDetector = new StepDetector(context);
    }

    /**
     * Zahaji skenovani na vsech adapterech (bt,wifi,gsm) po urcitou dobu. Pred skenovanim overi zda jsou pozadovane adaptery zapnuty a pripraveny, pokud ne, tak je zapne ale skenovani nespusti (vrati false).
     *
     * @param time               - jak dlouho ma byt sken spusten
     * @param scanResultListener - listener, ktery ma byt informovan o dokonceni skenovani a obdrzet vysledky
     * @return - zda bylo skenovani uspesne spusteno. False kdyz jsou nektere adaptery vypnute nebo skenovani uz bezi
     */
    public boolean startScan(int time, ScanResultListener scanResultListener) {
        return startScan(time, true, true, true, scanResultListener);
    }

    /**
     * Zahaji skenovani na pozadovanych adapterech po urcitou dobu. Pred skenovanim overi zda jsou pozadovane adaptery zapnuty a pripraveny, pokud ne, tak je zapne ale skenovani nespusti (vrati false).
     *
     * @param time               - jak dlouho ma byt sken spusten
     * @param wifi               - zda se maji skenovat dostupne wifi site
     * @param ble                - zda se maji skenovat dostupne ble beacony
     * @param cell               - zda se maji skenovat GSM "site" v dosahu
     * @param scanResultListener - listener, ktery ma byt informovan o dokonceni skenovani a obdrzet vysledky. Pro adaptery, na kterych nemelo byt skenovano vraci prazdny list a ne null
     * @return - zda bylo skenovani uspesne spusteno. False kdyz jsou nektere adaptery vypnute nebo skenovani uz bezi
     */
    public boolean startScan(final int time, boolean wifi, boolean ble, boolean cell, final ScanResultListener scanResultListener) {
        ble = ble && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE); //vyradime ble pokud ho zarizeni nema.
        if (running || !enableHW(wifi, ble)) {
            return false; //pokud jeste nedobehlo probihajici skenovani (nebo problemy pri zapinani HW), NEstartuj nove a vrat false
        }
        running = true;
        cont = true; //nastav aby se synchronni skenovani cyklicky spoustela znovu

        progressDialog = showProgressDialogFlag();

        wifiScans.clear();
        bleScans.clear();
        cellScans.clear();
        startTime = SystemClock.uptimeMillis(); //zaznamenej cas zacatku skenovani
        if (wifi) {
            //zaregistrovani receiveru pro wifi sken
            context.registerReceiver(wifiBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            wm.startScan();
        }
        if (ble) {
            //nabindovani altbeaconu pro ble skenovani = start skenovani
            beaconConsumer.bind();
        }
        if (cell) {
            //cyklicke spousteni skenovani GSM po urcite dobe
            new Timer(true).scheduleAtFixedRate(
                    new TimerTask() {
                        public void run() {
                            List<NeighboringCellInfo> cells = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getNeighboringCellInfo();
                            if (cells != null) {
                                cellScans.addAll(convertCellResults(cells));
                            }
                            if (!cont) {
                                cancel();
                            }
                        }
                    }, 0, C.SCAN_FINDER_TIME);
        }

        //casovac ukonceni skenovani
        timer = new Timer();
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        if (running && scanResultListener != null) {
                            scanResultListener.onScanFinished(wifiScans, bleScans, cellScans);
                        }
                        stopScan();
                    }
                }, time);
        return true;
    }

    /**
     * Zapne bt/wifi.
     *
     * @param wifi Jestli ma byt zapnuta wifi
     * @param ble  Jestli ma byt zapnut bt
     * @return Vraci true pokud jsou vsechny pozadovane adaptery zapnuty a pripraveny zacit skenovat
     */
    private boolean enableHW(boolean wifi, boolean ble) {
        if (ble) { //pokud nas zajima zapnuti BT
            final BluetoothAdapter btAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
            if (btAdapter != null && !btAdapter.isEnabled()) {
                alertDialog = new AlertDialog.Builder(context)
                .setTitle("Bluetooth")
                .setMessage("BT je vypnut. Pro zabírání musí být zapnut. Zapínám")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!btAdapter.enable()) {
                            Toast.makeText(context, "Chyba při zapínání BT", Toast.LENGTH_SHORT).show();
                        }
                        alertDialog = null;
                    }
                })
                .show();
                return false; //zajima nas zapnuti BT ale ten je off -> navrat false protoze se musi pockat na jeho asynchronni zapnuti
            }
        }
        if (wifi) { //zajima nas zapnuti wifi
            if (wm.isWifiEnabled()) {
                return true; //wifi je zapla a ok
            } else {
                if (!wm.setWifiEnabled(true)) {
                    Toast.makeText(context, "Chyba při zapínání WiFi", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        }
        return true; //chceme zapnout jen bt -> ktery je zaply protoze jsme dosli az sem
    }

    /**
     * Zastavi vsechna skenovani ale NEnavrati vysledky scanResultListeneru
     */
    public void stopScan() {
        if (!running) {
            return;
        }
        cont = false;
        timer.cancel();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        if (cdt != null) {
            // pokud by nekdo zrusil scan a zacal znova, cdt by stale dobihal
            cdt.cancel();
        }
        context.unregisterReceiver(wifiBroadcastReceiver);
        beaconConsumer.unBind();
        running = false;
    }
    /**
     * Prevede list ScanResultu na list WifiScanu (custom trida) s prevodem timestampu vzhledem k pocatku skenovani. Nepridava do celkoveho Listu skenu
     *
     * @param scanResults
     * @return
     */
    private List<WifiScan> convertWifiResults(List<ScanResult> scanResults) {
        List<WifiScan> wifiScans = new ArrayList<>();
        for (ScanResult scan : scanResults) {
            WifiScan wifiScan = new WifiScan(scan.SSID, scan.BSSID, scan.level, scan.frequency);
            //        wifiScan.setTime((scan.timestamp / 1000) - (startTime)); //scan.timestamp ne nekterych telefonech/verzich/??? hazi nesmysly a na jinych zase funguje perfektne
            wifiScan.setTime(SystemClock.uptimeMillis() - startTime);

            wifiScans.add(wifiScan);
            Log.d(C.LOG_WIFISCAN, scan.toString());
        }
        return wifiScans;
    }

    /**
     * Zpracuje obdrzeny vysledek Beacon na custom tridu BleScan (mj. prida timestamp). Zaroven prida do celkoveho listu skenu a zavole updateProgressDialog()
     *
     * @param scan
     */
    public void handleBleResult(Beacon scan) {
        BleScan bleScan = new BleScan();
        bleScan.setAddress(scan.getBluetoothAddress());
        bleScan.setRssi(scan.getRssi());
        bleScan.setUuid(scan.getId1().toString());
        bleScan.setMajor(scan.getId2().toInt());
        bleScan.setMinor(scan.getId3().toInt());
        bleScan.setTime(SystemClock.uptimeMillis() - startTime);
        bleScans.add(bleScan);
        Log.d(C.LOG_BLESCAN, bleScan.toString());
    }

    /**
     * Prevede list androidich NeigboringCellInfo instanci na custom CellScan s pridanim timestampu ale bez zavolani updateProgressDialog()
     *
     * @param cellResults - NeighboringCellInfo k prevodu
     * @return prevedene tridy
     */
    private List<CellScan> convertCellResults(List<NeighboringCellInfo> cellResults) {
        List<CellScan> cellScans = new ArrayList<>();
        for (NeighboringCellInfo cellResult : cellResults) {
            CellScan cellScan = new CellScan();
            cellScan.setTime(SystemClock.uptimeMillis() - startTime);
            cellScan.setRssi(cellResult.getRssi());
            cellScan.setType(cellResult.getNetworkType());
            cellScan.setCid(cellResult.getCid());
            cellScan.setLac(cellResult.getLac());
            cellScan.setPsc(cellResult.getPsc());
            cellScans.add(cellScan);
            Log.d(C.LOG_CELLSCAN, cellScan.toString());
        }
        return cellScans;
    }

    /**
     * Vytvori a zobrazi progressDialog pro informovani o postupu zabirani vlajky
     *
     * @return zobrazeny progressDialog
     */
    private ProgressDialog showProgressDialogFlag() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Zabírám vlajku, nehýbej se!");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                stopScan();
            }
        });
        updateProgressDialogFlag();
        return progressDialog;
    }

    /**
     * Updatuje zobrazovane hodnoty v progressDialogu pokud ten neni null.
     */
    private void updateProgressDialogFlag() {
        if (progressDialog != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stepDetector.enableStepDetector(true);
                    // -1500 aby se tam na chvíli zobrazilo hotovo, jinak to jen problikne
                    cdt = new CountDownTimer(C.SCAN_COLLECTOR_TIME - 1500, 1) {

                        public void onTick(long millisUntilFinished) {
                            // TODO animace zabirani vlajky
                            if (stepDetector.pohyb()) {
                                stopScan();
                                alertDialog = new AlertDialog.Builder(context)
                                        .setTitle("Nepodváděj!")
                                        .setMessage("Příliš jsi se pohl!")
                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                alertDialog = null;
                                                stepDetector.enableStepDetector(false);
                                            }
                                        })
                                        .show();
                                //  }
                            } else {
                                progressDialog.setMessage("Zbývá: " + millisUntilFinished / 1000f);
                            }
                        }

                        public void onFinish() {
                            progressDialog.setMessage("Hotovo!");
                        }
                    };
                    cdt.start();
                }
            });
        }
    }
}
