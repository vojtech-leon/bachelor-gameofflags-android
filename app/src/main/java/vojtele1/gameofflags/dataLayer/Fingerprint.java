package vojtele1.gameofflags.dataLayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Modelova trida reprezentujici fingerprint
 * Dominik Matoulek 2015
 */
public class Fingerprint {
    String level;

    int x;
    int y;
    String description;
    List<WifiScan> wifiScans = new ArrayList<>();
    List<BleScan> bleScans = new ArrayList<>();
    List<CellScan> cellScans = new ArrayList<>();

    // other recorded stuff...
    private float accX, accY, accZ, gyroX, gyroY, gyroZ, magX, magY, magZ;
    private String board, bootloader, brand, device, display, fingerprint, hardware, host, osId, manufacturer, model, product, serial, tags, type, user;
    private boolean supportsBLE;
    private String deviceID; // IMEI...

    private Float lat, lon;

    String createdDate;

    public Fingerprint() {
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public void setAccX(float accX) {
        this.accX = accX;
    }

    public void setAccY(float accY) {
        this.accY = accY;
    }

    public void setAccZ(float accZ) {
        this.accZ = accZ;
    }

    public void setGyroX(float gyroX) {
        this.gyroX = gyroX;
    }

    public void setGyroY(float gyroY) {
        this.gyroY = gyroY;
    }

    public void setGyroZ(float gyroZ) {
        this.gyroZ = gyroZ;
    }

    public void setMagX(float magX) {
        this.magX = magX;
    }

    public void setMagY(float magY) {
        this.magY = magY;
    }

    public void setMagZ(float magZ) {
        this.magZ = magZ;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public void setBootloader(String bootloader) {
        this.bootloader = bootloader;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public void setHardware(String hardware) {
        this.hardware = hardware;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setOsId(String osId) {
        this.osId = osId;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    @Override
    public String toString() {
        return "{" +
                ",\"level\":" + level +
                ",\"x\":" + x +
                ",\"y\":" + y +
                ",\"description\":" + "\"" + description + "\"" +
                ",\"wifiScans\":" + wifiScans +
                ",\"cellScans\":" + cellScans +
                ",\"supportsBLE\":" + supportsBLE +
                ",\"bleScans\":" + bleScans +
                ",\"accX\":" + accX +
                ",\"accY\":" + accY +
                ",\"accZ\":" + accZ +
                ",\"gyroX\":" + gyroX +
                ",\"gyroY\":" + gyroY +
                ",\"gyroZ\":" + gyroZ +
                ",\"magX\":" + magX +
                ",\"magY\":" + magY +
                ",\"magZ\":" + magZ +
                ",\"board\":" + "\"" + board + "\"" +
                ",\"bootloader\":" + "\"" + bootloader + "\"" +
                ",\"brand\":" + "\"" + brand + "\"" +
                ",\"device\":" + "\"" + device + "\"" +
                ",\"display\":" + "\"" + display + "\"" +
                ",\"fingerprint\":" + "\"" + fingerprint + "\"" +
                ",\"hardware\":" + "\"" + hardware + "\"" +
                ",\"host\":" + "\"" + host + "\"" +
                ",\"osId\":" + "\"" + osId + "\"" +
                ",\"manufacturer\":" + "\"" + manufacturer + "\"" +
                ",\"model\":" + "\"" + model + "\"" +
                ",\"product\":" + "\"" + product + "\"" +
                ",\"serial\":" + "\"" + serial + "\"" +
                ",\"tags\":" + "\"" + tags + "\"" +
                ",\"type\":" + "\"" + type + "\"" +
                ",\"user\":" + "\"" + user + "\"" +
                ",\"deviceID\":" + "\"" + deviceID + "\"" +
                ",\"lat\":" + lat +
                ",\"lon\":" + lon +
                ",\"createdDate\":" + createdDate +
                "}";
    }

    public void setWifiScans(List<WifiScan> wifiScans) {
        this.wifiScans = wifiScans;
    }

    public void setBleScans(List<BleScan> bleScans) {
        this.bleScans = bleScans;
    }

    public void setSupportsBLE(boolean supportsBLE) {
        this.supportsBLE = supportsBLE;
    }

    public void setCellScans(List<CellScan> cellScans) {
        this.cellScans = cellScans;
    }
}
