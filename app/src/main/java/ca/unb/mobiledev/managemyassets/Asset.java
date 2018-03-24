package ca.unb.mobiledev.managemyassets;

import java.io.Serializable;

/**
 * Created by Jason on 2018-02-04.
 */

public class Asset implements Serializable {

    private long id;
    private String name;
    private String description;
    private String notes;
    private String image;
    private double latitude;
    private double longitude;

    public static final String TABLE_NAME = "assets";
    public static final String OBJECT_NAME = "asset";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String NOTES = "notes";
    public static final String LAT = "latitude";
    public static final String LNG = "longitude";
    public static final String IMAGE = "image";
    public static final String DROP_TABLE_QUERY = "DROP TABLE IF EXISTS assets;";
    public static final String CREATE_TABLE_QUERY = "CREATE TABLE assets (id INTEGER PRIMARY KEY, name TEXT, description TEXT, notes TEXT, latitude REAL, longitude REAL, image TEXT);";

    public Asset() {
    }

    public Asset(String name, String description, String notes, double latitude, double longitude, String image) {
        this.name = name;
        this.description = description;
        this.notes = notes;
        this.latitude = latitude;
        this.longitude = longitude;
        this.image = image;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String toString() {
        return "<b>" + id + ". " + name + "</b><br><i>" + description + "</i><br>" + "Lat: " + latitude + " Lon: " + longitude;
    }
}
