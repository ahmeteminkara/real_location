import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';

import 'location_data.dart';

class RealLocation {
  static const MethodChannel _channel = const MethodChannel('real_location');

  static RealLocation get instanse => RealLocation();

  static StreamSubscription _subEventLocationEnable;
  static StreamSubscription _subEventTrackingLocation;
  static StreamSubscription _subEventLocation;
  static StreamSubscription _subPermissionResult;

  RealLocation() {
    _setListener();
  }

  _setListener() async {
    try {
      _subPermissionResult = EventChannel("eventPermissionResult").receiveBroadcastStream().listen((e) {
        // print("eventLocationEnable: $e");
        _listenPermissionResultController.add(e);
      });

      _subEventLocationEnable = EventChannel("eventLocationEnable").receiveBroadcastStream().listen((e) {
        // print("eventLocationEnable: $e");
        _listenEnableLocationController.add(e);
      });

      _subEventTrackingLocation = EventChannel("eventTrackingLocation").receiveBroadcastStream().listen((e) {
        // print("eventTrackingLocation: $e");
        _listenTrackingLocationController.add(e);
      });

      _subEventLocation = EventChannel("eventLocation").receiveBroadcastStream().listen((e) {
        try {
          Map<String, dynamic> json = jsonDecode(e);
          // print("eventLocation -> json: $json");
          _listenLocationController.add(LocationData(
            double.parse(json["latitude"].toString()),
            double.parse(json["longitude"].toString()),
            accuracy: double.parse(json["accuracy"].toString()),
            speed: double.parse(json["speed"].toString()),
          ));
        } catch (e) {
          // print("eventLocation -> error: "+ e.toString());
        }
      });
    } catch (e) {
      // print("Error: $e");
    }
  }

  Stream<bool> get listenTrackingLocation => _listenTrackingLocationController.stream;
  final _listenTrackingLocationController = StreamController<bool>();

  Stream<bool> get listenEnableLocation => _listenEnableLocationController.stream;
  final _listenEnableLocationController = StreamController<bool>();

  Stream<LocationData> get listenLocation => _listenLocationController.stream;
  final _listenLocationController = StreamController<LocationData>();

  Stream<bool> get listenPermissionResult => _listenPermissionResultController.stream;
  final _listenPermissionResultController = StreamController<bool>();

  Future<void> requestPermission() async => await _channel.invokeMethod("requestPermission");

  Future<bool> get isLocationEnable async => await _channel.invokeMethod("isLocationEnable");

  Future<void> startTracker() async => await _channel.invokeMethod("start");

  Future<void> stopTracker() async => await _channel.invokeMethod("stop");


  void dispose() {
    if (_subEventLocationEnable != null) _subEventLocationEnable.cancel();
    if (_subEventTrackingLocation != null) _subEventTrackingLocation.cancel();
    if (_subEventLocation != null) _subEventLocation.cancel();
    if (_subPermissionResult != null) _subPermissionResult.cancel();

    _listenLocationController.close();
    _listenEnableLocationController.close();
    _listenTrackingLocationController.close();
    _listenPermissionResultController.close();
  }
}
