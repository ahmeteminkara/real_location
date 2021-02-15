package com.ahmet.real_location;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.ahmet.real_location.RealLocationPlugin.TAG;

public class DeviceControls {

    public static final int requestCodeLocation = 1;
    public static final int locationResultCode = 3;

    @SuppressLint("StaticFieldLeak")

    /**
     * cihazda bluetooth u kontrol eder
     * @param activity
     */
    public static boolean isOpenBluetooth(Activity activity) {

        BluetoothManager bluetoothManager = (BluetoothManager)
                activity.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        return bluetoothAdapter.isEnabled();
    }

    /**
     * cihazda ble desteğini kontrol eder
     */
    public static boolean isSupportBle(Activity activity) {
        return activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * cihazın konumunu kontrol eder
     */
    public static boolean isOpenLocation(Activity activity) {

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
        }

        /*
        if (!isOpenBluetooth(activity)) {

            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableIntent, BleScanner.bluetoothRequestCode);
            return false;
        }
        if (!isSupportBle(activity)) {
            errorCallback.onDeviceError(BleDeviceErrors.UNSUPPORTED_BLUETOOTH_LE);
            return false;
        }
        */
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

