# Real Location

This flutter plugin listens for the location of the device. Returns real location, not mock location (for Android)

## Getting Started
```yaml
dependencies:
    ble_radar:
        git:
            url: git://github.com/ahmeteminkara/BleRadar.git
```

```dart
import 'package:real_location/real_location.dart';
```

## Variables
```java
RealLocation realLocation = RealLocation.instanse;
LocationData locationData;
bool isEnableLocation = false;
bool isTrackingLocation = false;


LocationData = { 
  latitude:double,
  longitude:double,
  speed:double,
  accuracy:double,
}
```


## Listeners
```dart
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

Timer(Duration(seconds: 1), realLocation.startTracker);
```

## Dispose
```java
@override
void dispose() {
    realLocation.dispose();
    super.dispose();
}
```

## Android Permission
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

## iOS Permission
```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string></string>
<key>NSLocationAlwaysUsageDescription</key>
<string></string>
<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
<string></string>
```
