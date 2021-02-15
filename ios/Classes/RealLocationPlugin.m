#import "RealLocationPlugin.h"
#if __has_include(<real_location/real_location-Swift.h>)
#import <real_location/real_location-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "real_location-Swift.h"
#endif

@implementation RealLocationPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftRealLocationPlugin registerWithRegistrar:registrar];
}
@end
