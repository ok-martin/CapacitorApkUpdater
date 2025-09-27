import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(CapacitorApkUpdaterPlugin)
public class CapacitorApkUpdaterPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "CapacitorApkUpdaterPlugin"
    public let jsName = "CapacitorApkUpdater"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "startApkDownload", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getDownloadStatus", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "canInstallApks", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "requestInstallPermission", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "cancelDownload", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "installApk", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getAppInfo", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = CapacitorApkUpdater()

    @objc func startApkDownload(_ call: CAPPluginCall) {
        call.resolve([
            "success": false,
            "error": "APK installation is not supported on iOS"
        ])
    }
    
    @objc func getDownloadStatus(_ call: CAPPluginCall) {
        call.resolve([
            "success": false,
            "bytesDownloaded": 0,
            "totalSize": 0,
            "progress": 0,
            "status": -1
        ])
    }
    
    @objc func canInstallApks(_ call: CAPPluginCall) {
        call.resolve([
            "canInstall": false,
            "reason": "APK installation is not supported on iOS"
        ])
    }
    
    @objc func requestInstallPermission(_ call: CAPPluginCall) {
        call.resolve([
            "granted": false
        ])
    }
    
    @objc func cancelDownload(_ call: CAPPluginCall) {
        call.resolve([
            "cancelled": false
        ])
    }
    
    @objc func installApk(_ call: CAPPluginCall) {
        call.resolve([
            "success": false,
            "error": "APK installation is not supported on iOS"
        ])
    }
    
    @objc func getAppInfo(_ call: CAPPluginCall) {
        // Get actual iOS app info instead of rejecting
        if let bundle = Bundle.main {
            let version = bundle.infoDictionary?["CFBundleShortVersionString"] as? String ?? "Unknown"
            let build = bundle.infoDictionary?["CFBundleVersion"] as? String ?? "0"
            let bundleId = bundle.bundleIdentifier ?? "Unknown"
            
            call.resolve([
                "success": true,
                "packageName": bundleId,
                "versionName": version,
                "versionCode": Int(build) ?? 0
            ])
        } else {
            call.resolve([
                "success": false,
                "error": "Unable to retrieve app information"
            ])
        }
    }
}
