package cz.uhk.fim.ringtonechanger;

/**
 * Created by Petr on 14. 1. 2016.
 */
public class Area {

    private int id;
    private String name;
    private String longitude;
    private String latitude;
    private float radius;
    private int active;
    private String ringtone;
    private String wifi;

    public Area() {
    }

    public Area(int id, String name, int active, String ringtone, String wifi) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.ringtone = ringtone;
        this.wifi = wifi;
    }

    public Area(int id, String name, String longitude, String latitude, float radius) {
        this.id = id;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.radius = radius;
    }

    public Area(int id, String name, String longitude, String latitude, float radius, int active, String ringtone, String wifi) {
        this.id = id;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.radius = radius;
        this.active = active;
        this.ringtone = ringtone;
        this.wifi = wifi;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public String getRingtone() {
        return ringtone;
    }

    public void setRingtone(String ringtone) {
        this.ringtone = ringtone;
    }

    public String getWifi() {
        return wifi;
    }

    public void setWifi(String wifi) {
        this.wifi = wifi;
    }
}
