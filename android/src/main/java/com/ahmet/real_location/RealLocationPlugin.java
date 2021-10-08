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
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.List;

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

import static android.content.Context.LOCATION_SERVICE;

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

    EventChannel.EventSink eventSinkLocation;
    EventChannel.EventSink eventSinkTrackingLocation;
    private MethodChannel channel;
    private Context context;
    private Activity activity;
    private LocationManager locationManager;


    /**
     * İlk önce onAttachedToEngine çalışır. Buradan context e ulaşabiliriz Sonra onAttachedToActivity çağırılır.
     */
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        //Log.i(TAG, "onAttachedToEngine");
        context = flutterPluginBinding.getApplicationContext();

        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "real_location");
        channel.setMethodCallHandler(this);

        BinaryMessenger messenger = flutterPluginBinding.getBinaryMessenger();

        new EventChannel(messenger, "eventLocationEnable")
                .setStreamHandler(new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object arguments, final EventChannel.EventSink events) {

                        events.success(DeviceControls.isOpenLocation(activity));

                        context.registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {

                                Log.d(TAG, "Açıldı kapandı");
                                DeviceControls.isOpenLocation(activity);

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
     * onAttachedToEngine ve onAttachedToActivity çalıştıktan sonra hemen çağırılır. işlem bu methoda gelince context ve activity elimizde demektir.
     */
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

        //Log.i(TAG, "onMethodCall");
        switch (call.method) {
            case "isLocationEnable":
                result.success(DeviceControls.isOpenLocation(activity));
                break;
            case "start":
                if (DeviceControls.checkSetting(activity)) {
                    locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
                    try {
                        eventSinkTrackingLocation.success(true);
                        List<String> providers = locationManager.getProviders(true);
                        for (String provider : providers) {
                            locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
                        }
                    } catch (Exception e) {
                        //Log.e(TAG, "timerLocation.schedule: " + e.toString());
                    }
                }
                break;
            case "stop":
                locationManager.removeUpdates(locationListener);
                eventSinkTrackingLocation.success(false);
                break;
        }
    }
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            if (location.isFromMockProvider()) {
                Log.e(TAG, "Location[" + location.getProvider() + "] isFromMockProvider !!!!!!");
                eventSinkLocation.success(null);
            } else {
                eventSinkLocation.success(new LocationData(location).toString());
                Log.d(TAG, "Location[" + location.getProvider() + "] -> " + location);
            }
        }
    };

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        //Log.i(TAG, "onDetachedFromEngine");
        channel.setMethodCallHandler(null);
    }

    /**
     * onAttachedToActivity den sonra burası gelir. Buradan activity ye ulaşabiliriz. Sonra onMethodCall çağırılır.
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


                //} else {
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
