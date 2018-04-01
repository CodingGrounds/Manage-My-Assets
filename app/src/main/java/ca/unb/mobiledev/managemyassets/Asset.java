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

    public Asset() {
    }

    public Asset(long id, String name, String description, String notes, double latitude, double longitude, String image) {
        this.id = id;
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
        return "<b>" + name + "</b><br><i>" + description + "</i><br>"
                + MMAConstants.ASSET_LATITUDE + ": " + latitude + " <br> "
                + MMAConstants.ASSET_LONGITUDE + ": " + longitude;
    }
}
