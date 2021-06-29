package com.ahmet.real_location;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * RealLocationPlugin
 */
public class RealLocationPlugin
        implements
        FlutterPlugin,
        MethodCallHandler,
        ActivityAware,
        PluginRegistry.ActivityResultListener,
        PluginRegistry.RequestPermissionsResultListener {

    public static final String TAG = RealLocationPlugin.class.getName();
    private final Handler handlerLocation = new Handler();
    EventChannel.EventSink eventSinkLocation;
    EventChannel.EventSink eventSinkTrackingLocation;
    private MethodChannel channel;
    private Context context;
    private Activity activity;
    private LocationManager locationManager;
    private final Runnable runnableLocation = new Runnable() {
        @Override
        public void run() {

            getLocationData();
            if (DeviceControls.checkSetting(activity))
                handlerLocation.postDelayed(runnableLocation, 1000);
        }
    };
    private BinaryMessenger messenger;


    /**
     * İlk önce onAttachedToEngine çalışır.
     * Buradan context e ulaşabiliriz
     * Sonra onAttachedToActivity çağırılır.
     */
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        //Log.i(TAG, "onAttachedToEngine");
        context = flutterPluginBinding.getApplicationContext();

        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "real_location");
        channel.setMethodCallHandler(this);

        messenger = flutterPluginBinding.getBinaryMessenger();

        new EventChannel(messenger, "eventLocationEnable")
                .setStreamHandler(new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object arguments, final EventChannel.EventSink events) {
                        context.registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {

                                //Log.d(TAG, "Açıldı kapandı");
                                boolean isOpen = DeviceControls.isOpenLocation(activity);
                                events.success(isOpen);

                            }
                        }, new IntentFilter(LocationManager.MODE_CHANGED_ACTION));
                    }

                    @Override
                    public void onCancel(Object arguments) {

                    }
                });

        new EventChannel(messenger, "eventLocation")
                .setStreamHandler(new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object arguments, EventChannel.EventSink events) {
                        eventSinkLocation = events;
                    }

                    @Override
                    public void onCancel(Object arguments) {
                        eventSinkLocation = null;
                    }
                });

        new EventChannel(messenger, "eventTrackingLocation")
                .setStreamHandler(new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object arguments, EventChannel.EventSink events) {
                        eventSinkTrackingLocation = events;
                    }

                    @Override
                    public void onCancel(Object arguments) {
                        eventSinkTrackingLocation = null;
                    }
                });

    }

    /**
     * onAttachedToEngine ve onAttachedToActivity çalıştıktan sonra hemen çağırılır.
     * işlem bu methoda gelince context ve activity elimizde demektir.
     */
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

        //Log.i(TAG, "onMethodCall");
        switch (call.method) {
            case "isLocationEnable":
                result.success(DeviceControls.isOpenLocation(activity));
                break;
            case "start":
                runOnUiThreadMethod();
                break;
            case "stop":
                handlerLocation.removeCallbacks(runnableLocation);
                eventSinkTrackingLocation.success(false);
                break;
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        //Log.i(TAG, "onDetachedFromEngine");
        channel.setMethodCallHandler(null);
    }

    /**
     * onAttachedToActivity den sonra burası gelir.
     * Buradan activity ye ulaşabiliriz.
     * Sonra onMethodCall çağırılır.
     */
    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        //Log.i(TAG, "onAttachedToActivity");
        activity = binding.getActivity();

        binding.addActivityResultListener(this);
        binding.addRequestPermissionsResultListener(this);


    }

    /**
     * onAttachedToActivity içine yazmazsan çalışmaz -> binding.addRequestPermissionsResultListener(this);
     */
    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == DeviceControls.requestCodeLocation) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                
                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Konum erişimi reddedildi");
                builder.setMessage("Konum erişimi izni vermeniz gerekmektedir");
                builder.setPositiveButton(android.R.string.ok, null);
                //builder.setOnDismissListener(dialog -> runOnUiThreadMethod());
                builder.show();

                 
            } else {
                //runOnUiThreadMethod();
            }
        }
        return false;
    }

    /**
     * onAttachedToActivity içine yazmazsan çalışmaz -> binding.addActivityResultListener(this);
     */
    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d(TAG, "onActivityResult");

        //Log.d(TAG, "requestCode " + requestCode + ", requestCodeLocation: " + resultCode);
        if (requestCode == DeviceControls.locationResultCode) {

            if (DeviceControls.isOpenLocation(activity)) {
                //eventSinkPermissionResult.success(true);
                //Toast.makeText(activity, "Konum açıldı", Toast.LENGTH_SHORT).show();
            } else {
                //eventSinkPermissionResult.success(false);
                
                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Konum devre dışı");
                builder.setMessage("Bu uygulamanın konum erişimine ihtiyacı var, lütfen konumu açınız");
                builder.setPositiveButton(android.R.string.ok, null);
                //builder.setOnDismissListener(dialog -> runOnUiThreadMethod());
                builder.show();
                
            }

        }
        return false;
    }

    private void runOnUiThreadMethod() {
        //Log.i(TAG, "runOnUiThreadMethod");

        if (DeviceControls.checkSetting(activity)) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            try {
                eventSinkTrackingLocation.success(true);
                handlerLocation.postDelayed(runnableLocation, 500);
            } catch (Exception e) {
                //Log.e(TAG, "timerLocation.schedule: " + e.toString());
            }

        }
    }

    @SuppressLint("WrongConstant")
    private void getLocationData() {
        if (locationManager == null) {
            //Log.e(TAG, "getLocationData -> locationManager is null");
            return;
        }


        try {
            Criteria criteria = new Criteria();

            criteria.setPowerRequirement(Criteria.POWER_HIGH);
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            criteria.setSpeedRequired(false);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(false);
            String bestProvider = locationManager.getBestProvider(criteria, true);


            Location bestLocation = locationManager.getLastKnownLocation(bestProvider);
            if (bestLocation != null && !bestLocation.isFromMockProvider()) {
                eventSinkLocation.success(new LocationData(bestLocation).toString());
                //Log.d(TAG, "bestLocation -> " + bestLocation);
                return;
            }
        } catch (Exception e) {
            //Log.e(TAG, "bestLocation: " + e.toString());
        }

        try {
            Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (gpsLocation != null && !gpsLocation.isFromMockProvider()) {
                eventSinkLocation.success(new LocationData(gpsLocation).toString());
                //Log.d(TAG, "gpsLocation -> " + gpsLocation);
                return;
            }
        } catch (Exception e) {
            //Log.e(TAG, "gpsLocation: " + e.toString());
        }
        try {
            Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (networkLocation != null && !networkLocation.isFromMockProvider()) {
                eventSinkLocation.success(new LocationData(networkLocation).toString());
                //Log.d(TAG, "networkLocation -> " + networkLocation);
                return;
            }
        } catch (Exception e) {
            //Log.e(TAG, "networkLocation: " + e.toString());
        }
        //Log.d(TAG, "No location info");

        eventSinkLocation.success(null);

    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        //Log.i(TAG, "onDetachedFromActivityForConfigChanges");
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        //Log.i(TAG, "onReattachedToActivityForConfigChanges");
    }

    @Override
    public void onDetachedFromActivity() {
        //Log.i(TAG, "onDetachedFromActivity");
    }


}
