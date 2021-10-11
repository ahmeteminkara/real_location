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

  RealLocation() {
    _setListener();
  }

  _setListener() async {
    try {
      _subEventLocationEnable = EventChannel("eventLocationEnable").receiveBroadcastStream().listen((e) {
        print("eventLocationEnable: $e");
        _listenEnableLocationController.add(e);
      });

      _subEventTrackingLocation = EventChannel("eventTrackingLocation").receiveBroadcastStream().listen((e) {
        // print("eventTrackingLocation: $e");
        _listenTrackingLocationController.add(e);
      });

      _subEventLocation = EventChannel("eventLocation").receiveBroadcastStream().listen((e) {
        try {
          if (e.runtimeType != String) return;
          Map<String, dynamic> json = jsonDecode(e);
          print("eventLocation -> json: $json");

          LocationData location = LocationData(
            double.parse(json["latitude"].toString()),
            double.parse(json["longitude"].toString()),
            accuracy: double.parse(json["accuracy"].toString()),
            speed: double.parse(json["speed"].toString()),
          );

          try {
            location.time = DateTime.fromMillisecondsSinceEpoch(int.parse(json["time"].toString()));
          } catch (e) {
            print("eventLocation location.time -> error: " + e.toString());
          }

          _listenLocationController.add(location);
        } catch (e) {
          print("eventLocation -> error: " + e.toString());
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

  Future<bool> get isLocationEnable async => await _channel.invokeMethod("isLocationEnable");

  Future<void> startTracker() async => await _channel.invokeMethod("start");

  Future<void> stopTracker() async => await _channel.invokeMethod("stop");

  void dispose() {
    if (_subEventLocationEnable != null) _subEventLocationEnable.cancel();
    if (_subEventTrackingLocation != null) _subEventTrackingLocation.cancel();
    if (_subEventLocation != null) _subEventLocation.cancel();

    _listenLocationController.close();
    _listenEnableLocationController.close();
    _listenTrackingLocationController.close();
  }
}
