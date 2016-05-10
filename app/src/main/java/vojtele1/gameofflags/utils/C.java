package vojtele1.gameofflags.utils;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

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



    public static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";

    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";

    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    public static final float GEOFENCE_RADIUS_IN_METERS = 1600; // 1 mile, 1.6 km

    /**
     * Map for storing information about airports in the San Francisco bay area.
     */
    public static final HashMap<String, LatLng> BAY_AREA_LANDMARKS = new HashMap<String, LatLng>();
    static {
        // San Francisco International Airport.
        BAY_AREA_LANDMARKS.put("Doma", new LatLng(50.505316, 16.007224));

        BAY_AREA_LANDMARKS.put("Škola", new LatLng(50.204474, 15.829622));



    }

}
