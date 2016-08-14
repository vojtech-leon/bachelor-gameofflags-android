package vojtele1.gameofflags.dataLayer;

/**
 * Trida reprezentujici zjistenou GSM BTS.
 * Created by Matej Danicek on 8.11.2015.
 */
public class CellScan {
    /**
     * CellID - identifikuje jednoznacne BTS (vysilac) popr jeho antenu uvnitr "Location Area" pro GPRS a EDGE
     */
    private int cid;
    /**
     * Identifikuje "Location Area", tedy BTSkovou oblast spadajici pod jeden "Base Controller" pro GPRS a EDGE
     */
    private int lac;
    /**
     * Identifikator pro UMTS,HSxxA
     */
    private int psc;
    private int rssi;
    /**
     * Typ GSM site podle konstant TelephonyManager.NETWORK_TYPE_xxx
     */
    private int type;
    /**
     * Ms casovy rozdil mezi zacatkem skenovani a obdrzenim tohoto vysledku
     */
    private long time;

    public CellScan() {
    }

    @Override
    public String toString() {
        return "{" +
                "\"cid\":" + "\"" + cid + "\"" +
                ",\"lac\":" + "\"" + lac + "\"" +
                ",\"psc\":" + "\"" + psc + "\"" +
                ",\"rssi\":" + rssi +
                ",\"type\":" + type +
                ",\"time\":" + time +
                "}";
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public void setLac(int lac) {
        this.lac = lac;
    }

    public void setPsc(int psc) {
        this.psc = psc;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
