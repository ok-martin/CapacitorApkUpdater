package com.spotgato.plugins.capacitorapkupdater;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import androidx.core.content.FileProvider;
import com.getcapacitor.Logger;

import java.io.File;

public class CapacitorApkUpdater {
    private Context context;
    private DownloadManager downloadManager;
    private BroadcastReceiver downloadReceiver;
    private DownloadListener downloadListener;
    private long currentDownloadId = 0;
    private Handler progressHandler;
    private Runnable progressRunnable;
    private boolean isMonitoringProgress = false;
    private boolean completionHandled = false;

    public interface DownloadListener {
        void onDownloadComplete(boolean success, String filePath, String error);
        void onDownloadProgress(long bytesDownloaded, long totalSize, int progress);
    }

    public CapacitorApkUpdater(Context context) {
        this.context = context;
        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        this.progressHandler = new Handler(Looper.getMainLooper());
    }

    public void setDownloadListener(DownloadListener listener) {
        this.downloadListener = listener;
    }

    public boolean startDownload(String url, String filename, boolean showNotification, String notificationTitle) throws Exception{
        if (url == null || url.isEmpty()) {
            throw new Exception("URL is required for download");
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(notificationTitle != null ? notificationTitle : "Downloading Update...");
        request.setDescription("Downloading app update");
        request.setNotificationVisibility(showNotification ? DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED : DownloadManager.Request.VISIBILITY_HIDDEN);

        String downloadFilename = filename != null ? filename : "update.apk";
        File downloadDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), downloadFilename);
        request.setDestinationUri(Uri.fromFile(downloadDir));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

        // Start download
        currentDownloadId = downloadManager.enqueue(request);

        // Reset completion flag for new download
        completionHandled = false;
        
        registerDownloadReceiver();
        startProgressMonitoring();
        return true;
    }

    private void registerDownloadReceiver() {
        if (downloadReceiver != null) {
            return; // Already registered
        }

        downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                Logger.info("CapacitorApkUpdater", "BroadcastReceiver triggered for download ID: " + id + ", current ID: " + currentDownloadId);
                if (id == currentDownloadId) {
                    Logger.info("CapacitorApkUpdater", "Download ID matches, calling handleDownloadComplete");
                    handleDownloadComplete();
                }
            }
        };

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        
        // For Android 13+ (API 33+), specify the receiver export flag
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(downloadReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(downloadReceiver, filter);
        }
    }

    private void handleDownloadComplete() {
        Logger.info("CapacitorApkUpdater", "handleDownloadComplete called, completionHandled: " + completionHandled);
        
        // Prevent duplicate completion handling
        if (completionHandled) {
            Logger.info("CapacitorApkUpdater", "Download completion already handled, skipping");
            return;
        }
        completionHandled = true;

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(currentDownloadId);
        Cursor cursor = downloadManager.query(query);

        if (cursor.moveToFirst()) {
            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
            int localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);

            int status = cursor.getInt(statusIndex);
            String localUri = cursor.getString(localUriIndex);

            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                if (downloadListener != null) {
                    downloadListener.onDownloadComplete(true, localUri, null);
                }
            } else {
                int reason = cursor.getInt(reasonIndex);
                String error = "Download failed with reason: " + reason;
                if (downloadListener != null) {
                    downloadListener.onDownloadComplete(false, null, error);
                }
            }
        } else {
            if (downloadListener != null) {
                downloadListener.onDownloadComplete(false, null, "Download not found");
            }
        }

        cursor.close();
        unregisterDownloadReceiver();
        stopProgressMonitoring();
    }

    private void startProgressMonitoring() {
        if (isMonitoringProgress || currentDownloadId == 0) {
            return;
        }

        isMonitoringProgress = true;
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentDownloadId != 0 && isMonitoringProgress) {
                    DownloadStatus status = getDownloadStatus();
                    
                    if (status.success && downloadListener != null) {
                        // Only notify if we're still downloading
                        if (status.status == DownloadManager.STATUS_RUNNING || status.status == DownloadManager.STATUS_PENDING) {
                            downloadListener.onDownloadProgress(status.bytesDownloaded, status.totalSize, status.progress);
                            
                            // Schedule next check in 500ms
                            progressHandler.postDelayed(this, 500);
                        } else if (status.status == DownloadManager.STATUS_SUCCESSFUL || status.status == DownloadManager.STATUS_FAILED) {
                            // Download completed or failed, stop monitoring and trigger completion
                            isMonitoringProgress = false;
                            
                            // Trigger completion callback if not already handled
                            if (!completionHandled && downloadListener != null) {
                                completionHandled = true;
                                if (status.status == DownloadManager.STATUS_SUCCESSFUL) {
                                    downloadListener.onDownloadComplete(true, status.filePath, null);
                                } else {
                                    downloadListener.onDownloadComplete(false, null, "Download failed");
                                }
                            }
                        } else {
                            // Continue monitoring for other statuses
                            progressHandler.postDelayed(this, 1000);
                        }
                    } else {
                        // Stop monitoring if we can't get status
                        isMonitoringProgress = false;
                    }
                }
            }
        };
        
        // Start monitoring after a short delay
        progressHandler.postDelayed(progressRunnable, 100);
    }

    private void stopProgressMonitoring() {
        isMonitoringProgress = false;
        if (progressHandler != null && progressRunnable != null) {
            progressHandler.removeCallbacks(progressRunnable);
        }
    }

    public DownloadStatus getDownloadStatus() {
        if (currentDownloadId == 0) {
            return new DownloadStatus(false, 0, 0, 0, DownloadManager.STATUS_FAILED, "No active download");
        }

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(currentDownloadId);
        Cursor cursor = downloadManager.query(query);

        if (cursor.moveToFirst()) {
            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
            int totalSizeIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
            int localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);

            int status = cursor.getInt(statusIndex);
            long bytesDownloaded = cursor.getLong(bytesDownloadedIndex);
            long totalSize = cursor.getLong(totalSizeIndex);
            String localUri = cursor.getString(localUriIndex);
            int progress = totalSize > 0 ? (int) ((bytesDownloaded * 100) / totalSize) : 0;

            cursor.close();
            return new DownloadStatus(true, bytesDownloaded, totalSize, progress, status, localUri);
        }

        cursor.close();
        return new DownloadStatus(false, 0, 0, 0, DownloadManager.STATUS_FAILED, "Download not found");
    }

    public InstallPermissionStatus canInstallApks() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean canInstall = context.getPackageManager().canRequestPackageInstalls();
            String reason = canInstall ? null : "Installation permission not granted";
            return new InstallPermissionStatus(canInstall, reason);
        } else {
            // For Android versions below 8.0, check if unknown sources is enabled
            boolean canInstall = Settings.Secure.getInt(context.getContentResolver(), 
                Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1;
            String reason = canInstall ? null : "Unknown sources not enabled";
            return new InstallPermissionStatus(canInstall, reason);
        }
    }

    public boolean cancelDownload() {
        if (currentDownloadId != 0) {
            downloadManager.remove(currentDownloadId);
            currentDownloadId = 0;
            unregisterDownloadReceiver();
            stopProgressMonitoring();
            return true;
        }
        return false;
    }

    public boolean installApk(String filePath) {
        try {
            File file = new File(Uri.parse(filePath).getPath());
            if (!file.exists()) {
                Logger.error("CapacitorApkUpdater", new Exception("APK file not found: " + filePath));
                return false;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkUri = FileProvider.getUriForFile(context, 
                    context.getPackageName() + ".fileprovider", file);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            }
            
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            
            return true;
            
        } catch (Exception e) {
            Logger.error("CapacitorApkUpdater", new Exception("Installation failed: " + e.getMessage()));
            return false;
        }
    }

    public AppInfo getAppInfo() {
        try {
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();
            String versionName = pm.getPackageInfo(packageName, 0).versionName;
            
            long versionCode;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = pm.getPackageInfo(packageName, 0).getLongVersionCode();
            } else {
                versionCode = pm.getPackageInfo(packageName, 0).versionCode;
            }
            
            return new AppInfo(true, packageName, versionName, versionCode, null);
            
        } catch (Exception e) {
            String error = "Failed to get app info: " + e.getMessage();
            Logger.error("CapacitorApkUpdater", new Exception(error));
            return new AppInfo(false, null, null, 0, error);
        }
    }

    public void cleanup() {
        unregisterDownloadReceiver();
        stopProgressMonitoring();
        currentDownloadId = 0;
    }

    private void unregisterDownloadReceiver() {
        if (downloadReceiver != null) {
            try {
                context.unregisterReceiver(downloadReceiver);
            } catch (IllegalArgumentException e) {
                // Receiver not registered
            }
            downloadReceiver = null;
        }
    }

    // Data classes for structured responses
    public static class DownloadStatus {
        public final boolean success;
        public final long bytesDownloaded;
        public final long totalSize;
        public final int progress;
        public final int status;
        public final String filePath;

        public DownloadStatus(boolean success, long bytesDownloaded, long totalSize, int progress, int status, String filePath) {
            this.success = success;
            this.bytesDownloaded = bytesDownloaded;
            this.totalSize = totalSize;
            this.progress = progress;
            this.status = status;
            this.filePath = filePath;
        }
    }

    public static class InstallPermissionStatus {
        public final boolean canInstall;
        public final String reason;

        public InstallPermissionStatus(boolean canInstall, String reason) {
            this.canInstall = canInstall;
            this.reason = reason;
        }
    }

    public static class AppInfo {
        public final boolean success;
        public final String packageName;
        public final String versionName;
        public final long versionCode;
        public final String error;

        public AppInfo(boolean success, String packageName, String versionName, long versionCode, String error) {
            this.success = success;
            this.packageName = packageName;
            this.versionName = versionName;
            this.versionCode = versionCode;
            this.error = error;
        }
    }
}
