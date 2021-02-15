import 'dart:convert';

class LocationData {
  double latitude = 0;
  double longitude = 0;
  double speed = 0;
  double accuracy = 0;

  LocationData(
    this.latitude,
    this.longitude, {
    this.speed = 0,
    this.accuracy = 0,
  });

  @override
  String toString() {
    return jsonEncode({
      "latitude": latitude,
      "longitude": longitude,
      "speed": speed,
      "accuracy": accuracy,
    });
  }
}
