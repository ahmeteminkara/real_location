import 'dart:convert';

class LocationData {
  double latitude = 0;
  double longitude = 0;
  double speed = 0;
  double accuracy = 0;
  DateTime time;

  LocationData(
    this.latitude,
    this.longitude, {
    this.speed = 0,
    this.accuracy = 0,
    this.time,
  });

  @override
  String toString() {
    return jsonEncode({
      "latitude": latitude,
      "longitude": longitude,
      "speed": speed.toStringAsFixed(0),
      "accuracy": accuracy.toStringAsFixed(0),
      "time": time,
    });
  }
}
