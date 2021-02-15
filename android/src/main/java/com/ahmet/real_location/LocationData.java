package com.ahmet.real_location;

import android.location.Location;

import org.json.JSONObject;

public class LocationData {
    double latitude;
    double longitude;
    double speed;
    double accuracy;

    public LocationData(Location location) {
        this.accuracy = location.getAccuracy();
        this.speed = location.getSpeed();
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
    }


    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);
            jsonObject.put("speed", speed);
            jsonObject.put("accuracy", accuracy);
        } catch (Exception ignored) {
        }
        return jsonObject.toString();
    }
}
