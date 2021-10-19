package com.ahmet.real_location;

import android.annotation.SuppressLint;
import android.location.Location;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class LocationData {
    long time;
    double latitude;
    double longitude;
    double speed;
    double accuracy;
    boolean isFake;

    public LocationData(Location location, boolean isFake) {
        this.accuracy = location.getAccuracy();
        this.speed = location.getSpeed();
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
        this.time = location.getTime();
        this.isFake = isFake;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);
            jsonObject.put("speed", speed);
            jsonObject.put("accuracy", accuracy);
            jsonObject.put("time", time);
            jsonObject.put("isFake", isFake);
            jsonObject.put("time_format", longToDateString());

        } catch (Exception ignored) {
        }
        return jsonObject.toString();
    }

    private String longToDateString() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return format.format(new Date(time));
        } catch (Exception e) {
            return "[!]";
        }

    }
}
