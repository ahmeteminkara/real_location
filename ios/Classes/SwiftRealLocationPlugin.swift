import Flutter
import UIKit
import CoreLocation



public class SwiftRealLocationPlugin: NSObject, FlutterPlugin, CLLocationManagerDelegate{
    
    var isEnableLocation = false
    var locationManager: CLLocationManager!
    
    var eventLocationEnable:CustomEventSink!
    var eventLocation:CustomEventSink!
    var eventTrackingLocation:CustomEventSink!
    
    public override init() {
        super.init()
        // print(" --> override init")
        
        locationManager = CLLocationManager()
        locationManager.delegate = self
        
        eventLocation = CustomEventSink()
        eventLocationEnable = CustomEventSink()
        eventTrackingLocation = CustomEventSink()
    }
    
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        // print(" --> register")
        
        
        
        let channel = FlutterMethodChannel(name: "real_location", binaryMessenger: registrar.messenger())
        let instance = SwiftRealLocationPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
        
        FlutterEventChannel(name: "eventLocationEnable", binaryMessenger: registrar.messenger())
            .setStreamHandler(instance.eventLocationEnable)
        
        FlutterEventChannel(name: "eventLocation", binaryMessenger: registrar.messenger())
            .setStreamHandler(instance.eventLocation)
        
        FlutterEventChannel(name: "eventTrackingLocation", binaryMessenger: registrar.messenger())
            .setStreamHandler(instance.eventTrackingLocation)
        
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        
        switch call.method {
        case "isLocationEnable":
            result(CLLocationManager.locationServicesEnabled())
            break
        case "start":
            if !isEnableLocation {
                print(" --> location disabled. app stoped")
                goToAppSetting()
                break
            }
            print(" --> startLocation")
            eventTrackingLocation.eventSink?(true)
            locationManager.desiredAccuracy = kCLLocationAccuracyBest
            locationManager.requestAlwaysAuthorization()
            locationManager.startUpdatingLocation()
            break
        case "stop":
            //code
            eventTrackingLocation.eventSink?(false)
            locationManager.stopUpdatingLocation()
            break
        default:
            break
        }
    }
    
    public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        
        
        isEnableLocation = CLLocationManager.locationServicesEnabled()
        eventLocationEnable.eventSink?(isEnableLocation)
        
        if !isEnableLocation {
            eventLocation.eventSink?(false)
            return
        }
        
        var dictionary =  [String:Any]()
        eventLocation.eventSink?(true)
        
        locations.forEach { (location:CLLocation ) in
            dictionary.removeAll()
            dictionary.updateValue(location.coordinate.latitude, forKey: "latitude")
            dictionary.updateValue(location.coordinate.longitude, forKey: "longitude")
            dictionary.updateValue(location.verticalAccuracy, forKey: "accuracy")
            dictionary.updateValue(location.speed, forKey: "speed")
            
            if let theJSONData = try? JSONSerialization.data(
                withJSONObject: dictionary,
                options: []) {
                let theJSONText = String(data: theJSONData,encoding: .ascii)
                eventLocation.eventSink?(theJSONText)
            }
            
        }
        
        
    }
    
    public func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        
        switch status {
        case .authorizedAlways, .authorizedWhenInUse:
            isEnableLocation = true
            locationManager.startUpdatingLocation()
            break
        case .notDetermined, .restricted:
            locationManager.stopUpdatingLocation()
            locationManager.requestAlwaysAuthorization()
            break
        case .denied:
            goToAppSetting()
            break
        default:
            break
        }
        
        eventLocationEnable.eventSink?(isEnableLocation)
        // print(" --> didChangeAuthorization isEnableLocation: ",isEnableLocation)
    }
    
    public func goToAppSetting(){
        // print(" --> goToAppSetting")
        DispatchQueue.main.async {
            
            let url = URL(string:UIApplication.openSettingsURLString)
            
            let alert = UIAlertController(title: "Uyarı", message: "Konum erişimi kapalı, uygulamaya konum erişimi veriniz.", preferredStyle:.alert)
            alert.addAction(UIAlertAction(title: "Tamam", style: .default, handler: {(cAlertAction) in 
            
                if #available(iOS 10.0, *) {
                    UIApplication.shared.open(url!)
                } else {
                    UIApplication.shared.openURL(url!)
                }
            
            }))
            UIApplication.shared.keyWindow?.rootViewController?.present(alert, animated: true, completion: nil);
        }
    }
    
}



class CustomEventSink: NSObject, FlutterStreamHandler {
    public var eventSink: FlutterEventSink?;
    
    func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        eventSink = events;
        return nil;
    }
    
    func onCancel(withArguments arguments: Any?) -> FlutterError? {
        eventSink = nil;
        return nil;
    }
    
}


