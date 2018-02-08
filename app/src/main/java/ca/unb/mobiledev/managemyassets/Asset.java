package ca.unb.mobiledev.managemyassets;

/**
 * Created by Jason on 2018-02-04.
 */

public class Asset {

    public String name;
    public String description;
    public double latitude;
    public double longitude;

    public static final String TABLE_NAME = "assets";
    public static final String DROP_TABLE_QUERY  = "DROP TABLE IF EXISTS assets;";
    public static final String CREATE_TABLE_QUERY = "CREATE TABLE assets (id INTEGER PRIMARY KEY, name TEXT, description TEXT, latitude REAL, longitude REAL, UNIQUE (name, latitude, longitude));";

    public Asset() {}

    public Asset(String name, String description, double latitude, double longitude) {
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
