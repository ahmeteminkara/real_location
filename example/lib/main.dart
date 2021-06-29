import 'dart:async';

import 'package:flutter/material.dart';

import 'package:real_location/real_location.dart';
import 'package:real_location/location_data.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  RealLocation realLocation = RealLocation.instanse;
  LocationData locationData;
  bool permissionStatus = false;
  bool isEnableLocation = false;
  bool isTrackingLocation = false;

  @override
  void initState() {
    super.initState();


    realLocation.listenEnableLocation.listen((bool isOpen) {
      setState(() => isEnableLocation = isOpen);
    });

    realLocation.listenLocation.listen((LocationData data) {
      if (data == null) return;
      setState(() => locationData = data);
    });

    realLocation.listenTrackingLocation.listen((bool isTracking) {
      setState(() => isTrackingLocation = isTracking);
    });
  }

  @override
  void dispose() {
    realLocation.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          backgroundColor: Colors.red[700],
          title: const Text('Flutter Location Plugin'),
          centerTitle: true,
        ),
        body: Container(
          alignment: Alignment.center,
          child: Column(mainAxisSize: MainAxisSize.min, children: bodyChildren),
        ),
      ),
    );
  }

  get bodyChildren {
    List<Widget> list = [];
    list.add(Text("permissionStatus: $permissionStatus"));

    list.add(Text("isEnableLocation: $isEnableLocation"));

    list.add(Text("isTrackingLocation: $isTrackingLocation"));

    if (locationData != null) {
      list.add(Text("${locationData.latitude.toStringAsFixed(5)},${locationData.longitude.toStringAsFixed(5)}"));
    }


    list.add(TextButton(
      onPressed: () => realLocation.startTracker(),
      child: Text("startTracker"),
    ));

    list.add(TextButton(
      onPressed: () => realLocation.stopTracker(),
      child: Text("stopTracker"),
    ));

    return list;
  }
}
