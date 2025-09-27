package com.spotgato.plugins.capacitorapkupdater;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.PluginResult;
import com.getcapacitor.Logger;

@CapacitorPlugin(name = "CapacitorApkUpdater")
public class CapacitorApkUpdaterPlugin extends Plugin {

    private CapacitorApkUpdater implementation;

    @Override
    public void load() {
        implementation = new CapacitorApkUpdater(getContext());
        implementation.setDownloadListener(new CapacitorApkUpdater.DownloadListener() {
            @Override
            public void onDownloadComplete(boolean success, String filePath, String error) {
                JSObject result = new JSObject();
                result.put("success", success);
                if (success) {
                    result.put("filePath", filePath);
                    boolean installed = implementation.installApk(filePath);
                    result.put("installed", installed);
                } else {
                    result.put("error", error);
                }
                
                try {
                    notifyListeners("downloadComplete", result);
                } catch (Exception e) {
                    Logger.error("CapacitorApkUpdater", new Exception("Error notifying downloadComplete listeners: " + e.getMessage()));
                }
            }

            @Override
            public void onDownloadProgress(long bytesDownloaded, long totalSize, int progress) {
                JSObject result = new JSObject();
                result.put("progress", progress);
                result.put("bytesDownloaded", bytesDownloaded);
                result.put("totalSize", totalSize);
                
                try {
                    notifyListeners("downloadProgress", result);
                } catch (Exception e) {
                    Logger.error("CapacitorApkUpdater", new Exception("Error notifying downloadProgress listeners: " + e.getMessage()));
                }
            }
        });
    }

    @Override
    protected void handleOnDestroy() {
        if (implementation != null) {
            implementation.cleanup();
        }
        super.handleOnDestroy();
    }

    @PluginMethod
    public void startApkDownload(PluginCall call) {
        JSObject result = new JSObject();

        try {
            String url = call.getString("url");
            if (url == null || url.isEmpty()) {
                call.reject("URL is required");
                return;
            }

            String filename = call.getString("filename", "update.apk");
            boolean showNotification = call.getBoolean("showNotification", true);
            String notificationTitle = call.getString("notificationTitle", "Downloading Update...");

            implementation.startDownload(url, filename, showNotification, notificationTitle);

            result.put("success", true);
            result.put("message", "Download started");
            call.resolve(result);
        } catch (Exception exception) {
            String errorMsg = "Download failed: " + exception.getMessage();
            Logger.error("CapacitorApkUpdater", new Exception(errorMsg));
            result.put("success", false);
            result.put("error", errorMsg);
        }

        call.resolve(result);
    }

    @PluginMethod
    public void getDownloadStatus(PluginCall call) {
        CapacitorApkUpdater.DownloadStatus status = implementation.getDownloadStatus();
        
        JSObject result = new JSObject();
        result.put("success", status.success);
        result.put("bytesDownloaded", status.bytesDownloaded);
        result.put("totalSize", status.totalSize);
        result.put("progress", status.progress);
        result.put("status", status.status);
        
        if (status.filePath != null) {
            result.put("filePath", status.filePath);
        }
        
        call.resolve(result);
    }

    @PluginMethod
    public void canInstallApks(PluginCall call) {
        CapacitorApkUpdater.InstallPermissionStatus status = implementation.canInstallApks();
        
        JSObject result = new JSObject();
        result.put("canInstall", status.canInstall);
        if (status.reason != null) {
            result.put("reason", status.reason);
        }
        
        call.resolve(result);
    }

    @PluginMethod
    public void requestInstallPermission(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getContext().getPackageManager().canRequestPackageInstalls()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                    .setData(Uri.parse(String.format("package:%s", getContext().getPackageName())));
                startActivityForResult(call, intent, "installPermissionResult");
                return;
            }
        }
        
        JSObject result = new JSObject();
        result.put("granted", true);
        call.resolve(result);
    }

    @PluginMethod
    public void cancelDownload(PluginCall call) {
        boolean cancelled = implementation.cancelDownload();
        
        JSObject result = new JSObject();
        result.put("cancelled", cancelled);
        call.resolve(result);
    }

    @PluginMethod
    public void installApk(PluginCall call) {
        String filePath = call.getString("filePath");
        if (filePath == null || filePath.isEmpty()) {
            call.reject("File path is required");
            return;
        }

        boolean success = implementation.installApk(filePath);
        
        JSObject result = new JSObject();
        result.put("success", success);
        if (success) {
            result.put("message", "Installation intent launched");
        } else {
            result.put("error", "Failed to launch installation");
        }
        
        call.resolve(result);
    }

    @PluginMethod
    public void getAppInfo(PluginCall call) {
        CapacitorApkUpdater.AppInfo appInfo = implementation.getAppInfo();
        
        JSObject result = new JSObject();
        result.put("success", appInfo.success);
        
        if (appInfo.success) {
            result.put("packageName", appInfo.packageName);
            result.put("versionName", appInfo.versionName);
            result.put("versionCode", appInfo.versionCode);
        } else {
            result.put("error", appInfo.error);
        }
        
        call.resolve(result);
    }

    @ActivityCallback
    private void installPermissionResult(PluginCall call, PluginResult result) {
        JSObject jsResult = new JSObject();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean granted = getContext().getPackageManager().canRequestPackageInstalls();
            jsResult.put("granted", granted);
        } else {
            jsResult.put("granted", true);
        }
        
        call.resolve(jsResult);
    }

    @Override
    protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        super.handleOnActivityResult(requestCode, resultCode, data);
        
        PluginCall savedCall = getSavedCall();
        if (savedCall == null) {
            return;
        }
        
        JSObject result = new JSObject();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean granted = getContext().getPackageManager().canRequestPackageInstalls();
            result.put("granted", granted);
        } else {
            result.put("granted", true);
        }
        
        savedCall.resolve(result);
    }
}
