import 'dart:convert';

class LocationData {
  double latitude = 0;
  double longitude = 0;
  double speed = 0;
  double accuracy = 0;
  DateTime time;
  bool isFake;

  LocationData(
    this.latitude,
    this.longitude, {
    this.speed = 0,
    this.accuracy = 0,
    this.time,
    this.isFake = false,
  });

  @override
  String toString() {
    if (isFake) {
      return jsonEncode({"latitude": latitude, "longitude": longitude, "isFake": isFake});
    }
    return jsonEncode({
      "latitude": latitude,
      "longitude": longitude,
      "speed": speed.toStringAsFixed(0),
      "accuracy": accuracy.toStringAsFixed(0),
      "time": time.toString()
    });
  }
}
