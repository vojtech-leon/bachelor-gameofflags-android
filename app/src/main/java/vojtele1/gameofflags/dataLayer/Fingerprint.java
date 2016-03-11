package vojtele1.gameofflags.dataLayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Modelova trida reprezentujici fingerprint
 * Dominik Matoulek 2015
 */
public class Fingerprint {
    // couchDB identificator
    String id;
    String level;

    int x;
    int y;
    String description = "";
    List<WifiScan> wifiScans = new ArrayList<>();
    List<BleScan> bleScans = new ArrayList<>();
    List<CellScan> cellScans = new ArrayList<>();

    // other recorded stuff...
    private float accX, accY, accZ, gyroX, gyroY, gyroZ, magX, magY, magZ;
    private String board, bootloader, brand, device, display, fingerprint, hardware, host, osId, manufacturer, model, product, serial, tags, type, user;
    private boolean supportsBLE;
    private String deviceID; // IMEI...

    private Float lat, lon;

    Date createdDate;

    public Fingerprint() {
    }

    public Fingerprint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void addScan(WifiScan s) {
        wifiScans.add(s);
    }

    public void addBleScan(BleScan s) {
        bleScans.add(s);
    }

    public void addCellScan(CellScan s){
        cellScans.add(s);
    }

    public List<WifiScan> getWifiScans() {
        return wifiScans;
    }

    public int getX() {

        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public WifiScan getScan(int index) {
        return wifiScans.get(index);
    }

    public float getAccX() {
        return accX;
    }

    public void setAccX(float accX) {
        this.accX = accX;
    }

    public float getAccY() {
        return accY;
    }

    public void setAccY(float accY) {
        this.accY = accY;
    }

    public float getAccZ() {
        return accZ;
    }

    public void setAccZ(float accZ) {
        this.accZ = accZ;
    }

    public float getGyroX() {
        return gyroX;
    }

    public void setGyroX(float gyroX) {
        this.gyroX = gyroX;
    }

    public float getGyroY() {
        return gyroY;
    }

    public void setGyroY(float gyroY) {
        this.gyroY = gyroY;
    }

    public float getGyroZ() {
        return gyroZ;
    }

    public void setGyroZ(float gyroZ) {
        this.gyroZ = gyroZ;
    }

    public float getMagX() {
        return magX;
    }

    public void setMagX(float magX) {
        this.magX = magX;
    }

    public float getMagY() {
        return magY;
    }

    public void setMagY(float magY) {
        this.magY = magY;
    }

    public float getMagZ() {
        return magZ;
    }

    public void setMagZ(float magZ) {
        this.magZ = magZ;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public String getBootloader() {
        return bootloader;
    }

    public void setBootloader(String bootloader) {
        this.bootloader = bootloader;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getHardware() {
        return hardware;
    }

    public void setHardware(String hardware) {
        this.hardware = hardware;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getOsId() {
        return osId;
    }

    public void setOsId(String osId) {
        this.osId = osId;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public Float getLat() {
        return lat;
    }

    public void setLat(Float lat) {
        this.lat = lat;
    }

    public Float getLon() {
        return lon;
    }

    public void setLon(Float lon) {
        this.lon = lon;
    }

    @Override
    public String toString() {
        return "Position{" +
                "id='" + id + '\'' +
                ", level=" + level +
                ", x=" + x +
                ", y=" + y +
                ", description='" + description + '\'' +
                ", wifiScans=" + wifiScans +
                ", cellScans=" + cellScans +
                ", supportsBLE=" + supportsBLE +
                ", bleScans=" + bleScans +
                ", accX=" + accX +
                ", accY=" + accY +
                ", accZ=" + accZ +
                ", gyroX=" + gyroX +
                ", gyroY=" + gyroY +
                ", gyroZ=" + gyroZ +
                ", magX=" + magX +
                ", magY=" + magY +
                ", magZ=" + magZ +
                ", board='" + board + '\'' +
                ", bootloader='" + bootloader + '\'' +
                ", brand='" + brand + '\'' +
                ", device='" + device + '\'' +
                ", display='" + display + '\'' +
                ", fingerprint='" + fingerprint + '\'' +
                ", hardware='" + hardware + '\'' +
                ", host='" + host + '\'' +
                ", osId='" + osId + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", model='" + model + '\'' +
                ", product='" + product + '\'' +
                ", serial='" + serial + '\'' +
                ", tags='" + tags + '\'' +
                ", type='" + type + '\'' +
                ", user='" + user + '\'' +
                ", deviceID='" + deviceID + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", createdDate=" + createdDate +
                '}';
    }

    public void setWifiScans(List<WifiScan> wifiScans) {
        this.wifiScans = wifiScans;
    }

    public List<BleScan> getBleScans() {
        return bleScans;
    }

    public void setBleScans(List<BleScan> bleScans) {
        this.bleScans = bleScans;
    }

    public boolean getSupportsBLE() {
        return supportsBLE;
    }

    public void setSupportsBLE(boolean supportsBLE) {
        this.supportsBLE = supportsBLE;
    }

    public List<CellScan> getCellScans() {
        return cellScans;
    }

    public void setCellScans(List<CellScan> cellScans) {
        this.cellScans = cellScans;
    }
}
