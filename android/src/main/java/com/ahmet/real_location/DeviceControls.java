package com.ahmet.real_location;


import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

public class DeviceControls {

    public static final int requestCodeLocation = 1;
    public static final int locationResultCode = 3;


    /**
     * cihazın konumunu kontrol eder
     */
    @SuppressLint("StaticFieldLeak")
    public static boolean isOpenLocation(Activity activity) {
        try {

            //Log.i(TAG, "isOpenLocation");
            boolean status;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                status = lm.isLocationEnabled();
            } else {
                int mode = Settings.Secure.getInt(activity.getContentResolver(), Settings.Secure.LOCATION_MODE,
                        Settings.Secure.LOCATION_MODE_OFF);
                status = (mode != Settings.Secure.LOCATION_MODE_OFF);
            }

            return status;

        } catch (Exception e) {
            return false;
        }

    }

    /**
     * konum izninin verilip verilmediğini kontrol eder
     */
    public static boolean isLocationPermission(Activity activity) {
        //Log.i(TAG, "isLocationPermission");
        boolean status = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{
                                ACCESS_COARSE_LOCATION,
                                ACCESS_FINE_LOCATION
                        },
                        requestCodeLocation);
                status = false;
            }
        }
        return status;
    }

    /**
     * uygulama için cihazda gerekli ayarlamaları kontrol eder
     */
    public static boolean checkSetting(Activity activity) {
        //Log.i(TAG, "checkSetting");
        if (activity == null) {
            //Log.d(TAG, "activity is null <checkSetting>");
            return false;
        }

        if (!isLocationPermission(activity)) {
            return false;
        }
        if (!isOpenLocation(activity)) {
            Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activity.startActivityForResult(viewIntent, locationResultCode);
            return false;
        }

        return true;
    }
}

