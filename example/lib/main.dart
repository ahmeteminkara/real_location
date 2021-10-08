import 'package:flutter/material.dart';

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
  LocationData locationData;
  bool isEnableLocation = false;
  bool isTrackingLocation = false;

  @override
  void initState() {
    super.initState();
    
  }

  @override
  void dispose() {
    realLocation.dispose();
    super.dispose();
  }

initRealLocation(){

    realLocation = RealLocation.instanse;

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
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.red[700],
        title: const Text('Flutter Location Plugin'),
        centerTitle: true,
      ),
      body: Container(
        alignment: Alignment.center,
        child: Column(mainAxisSize: MainAxisSize.min, children: bodyChildren),
      ),
    );
  }

  get bodyChildren {
    List<Widget> list = [];
  
    list.add(Text("isEnableLocation: $isEnableLocation"));

    list.add(Text("isTrackingLocation: $isTrackingLocation"));

    if (locationData != null) {
      list.add(Text("${locationData.latitude.toStringAsFixed(5)},${locationData.longitude.toStringAsFixed(5)}"));
    } else {
      list.add(Text(""));
    }

    list.add(TextButton(
      onPressed: () => initRealLocation(),
      child: Text("init"),
    ));

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
