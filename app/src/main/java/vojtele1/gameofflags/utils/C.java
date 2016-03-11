package vojtele1.gameofflags.utils;

/**
 * Trida obsahujici Configuracni a Constantni hodnoty
 * <p/>
 * Dominik Matoulek 2015
 */
public class C {
    public static final String LOG_BLESCAN = "BLE Scan";
    public static final String LOG_WIFISCAN = "Wifi Scan";
    public static final String LOG_CELLSCAN = "GSM Scan";
    /**
     * Jak dlouho ma probihat skenovani pri sberu dat
     */
    public static int SCAN_COLLECTOR_TIME = 20000;


    /**
     * Jak casto ma byt skenovano pri hledani (cle)
     */
    public static int SCAN_FINDER_TIME = 5000;


}
