import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

import 'package:real_location/real_location.dart';
import 'package:real_location/location_data.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      themeMode: ThemeMode.system,
      home: MainPage(),
    );
  }
}

class MainPage extends StatefulWidget {
  @override
  _MainPageState createState() => _MainPageState();
}

class _MainPageState extends State<MainPage> {
  RealLocation realLocation;
  LocationData locationData = LocationData(0, 0);
  bool isEnableLocation = false;
  bool isTrackingLocation = false;

  MapController mapController = MapController();

  @override
  void initState() {
    super.initState();

    Timer(Duration.zero, () => initRealLocation());
  }

  @override
  void dispose() {
    realLocation.dispose();
    super.dispose();
  }

  initRealLocation() {
    realLocation = RealLocation.instanse;

    realLocation.listenEnableLocation.listen((bool isOpen) {
      setState(() => isEnableLocation = isOpen);
    });

    realLocation.listenLocation.listen((LocationData data) {
      if (data == null) return;
      setState(() => locationData = data);
      centerMap(data);
    });

    realLocation.listenTrackingLocation.listen((bool isTracking) {
      setState(() => isTrackingLocation = isTracking);
    });

    realLocation.startTracker();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.red[700],
        title: const Text('Flutter Location Plugin'),
        centerTitle: true,
      ),
      body: Container(
        alignment: Alignment.center,
        child: Column(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: bodyChildren),
      ),
      persistentFooterButtons: [
        TextButton(
          onPressed: () => realLocation.startTracker(),
          child: Text("startTracker"),
        ),
        TextButton(
          onPressed: () => realLocation.stopTracker(),
          child: Text("stopTracker"),
        )
      ],
    );
  }

  get map {
    return FlutterMap(
      mapController: mapController,
      options: MapOptions(
        center: LatLng(locationData.latitude, locationData.longitude),
        zoom: 15.0,
      ),
      layers: [
        TileLayerOptions(
          urlTemplate: "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
          subdomains: ['a', 'b', 'c'],
          attributionBuilder: (_) {
            return Container();
          },
        ),
        MarkerLayerOptions(
          markers: [
            Marker(
              width: 80.0,
              height: 80.0,
              point: LatLng(locationData.latitude, locationData.longitude),
              builder: (ctx) => Container(
                child: Icon(Icons.lens_outlined),
              ),
            ),
          ],
        ),
      ],
    );
  }

  get bodyChildren {
    List<Widget> list = [];

    list.add(Container(
      width: double.infinity,
      height: 300,
      child: map,
    ));

    list.add(Text("isEnableLocation: $isEnableLocation"));
    list.add(Text("isTrackingLocation: $isTrackingLocation"));
    //list.add(Text("${locationData.latitude.toStringAsFixed(5)},${locationData.longitude.toStringAsFixed(5)}"));
    list.add(Text("accuracy: " + locationData.accuracy.toStringAsFixed(0)));

    if (locationData.time != null) {
      list.add(Text("time: " + locationData.time.toString()));
      final int diff = DateTime.now().difference(locationData.time).inMilliseconds;
      list.add(Text("time diff: $diff Milliseconds"));
    }

    list.add(SizedBox(height: 10));
    return list;
  }

  void centerMap(LocationData data) {
    if (mapController == null) return;
    mapController.move(LatLng(data.latitude, data.longitude), 16);
  }
}
