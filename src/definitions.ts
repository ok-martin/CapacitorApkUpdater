export interface CapacitorApkUpdaterPlugin {
  /**
   * Download and install APK from the provided URL
   */
  startApkDownload(options: DownloadApkOptions): Promise<DownloadApkResult>;

  /**
   * Get the current download status and progress
   */
  getDownloadStatus(): Promise<DownloadStatusResult>;

  /**
   * Check if the app can install APKs (has necessary permissions)
   */
  canInstallApks(): Promise<CanInstallResult>;

  /**
   * Request permission to install APKs (for Android 8.0+)
   */
  requestInstallPermission(): Promise<PermissionResult>;

  /**
   * Cancel ongoing download
   */
  cancelDownload(): Promise<CancelDownloadResult>;

  /**
   * Install APK from a specific file path
   */
  installApk(options: { filePath: string }): Promise<InstallApkResult>;

  /**
   * Get current app information (version, package name, etc.)
   */
  getAppInfo(): Promise<AppInfoResult>;

  /**
   * Add listener for download progress events
   */
  addListener(
    eventName: 'downloadProgress',
    listenerFunc: (event: DownloadProgressEvent) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Add listener for download completion events
   */
  addListener(
    eventName: 'downloadComplete',
    listenerFunc: (event: DownloadCompleteEvent) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * Remove all listeners for this plugin
   */
  removeAllListeners(): Promise<void>;
}

export interface PluginListenerHandle {
  remove(): Promise<void>;
}

export interface DownloadApkOptions {
  /**
   * URL to download the APK from
   */
  url: string;

  /**
   * Optional filename for the downloaded APK
   * Default: 'update.apk'
   */
  filename?: string;

  /**
   * Whether to show download progress notification
   * Default: true
   */
  showNotification?: boolean;

  /**
   * Notification title when downloading
   * Default: 'Downloading Update...'
   */
  notificationTitle?: string;
}

export interface DownloadApkResult {
  /**
   * Whether the download start was successful
   */
  success: boolean;

  /**
   * Success or error message
   */
  message?: string;

  /**
   * Error message if download failed to start
   */
  error?: string;
}

export interface DownloadStatusResult {
  /**
   * Whether the status query was successful
   */
  success: boolean;

  /**
   * Bytes downloaded so far
   */
  bytesDownloaded: number;

  /**
   * Total bytes to download
   */
  totalSize: number;

  /**
   * Download progress percentage (0-100)
   */
  progress: number;

  /**
   * Android DownloadManager status code
   */
  status: number;

  /**
   * Path to the downloaded file (when complete)
   */
  filePath?: string;
}

export interface CanInstallResult {
  /**
   * Whether the app can install APKs
   */
  canInstall: boolean;

  /**
   * Reason if installation is not possible
   */
  reason?: string;
}

export interface PermissionResult {
  /**
   * Whether permission was granted
   */
  granted: boolean;
}

export interface CancelDownloadResult {
  /**
   * Whether the download was successfully cancelled
   */
  cancelled: boolean;
}

export interface InstallApkResult {
  /**
   * Whether the installation intent was successfully launched
   */
  success: boolean;

  /**
   * Success or error message
   */
  message?: string;

  /**
   * Error message if installation failed
   */
  error?: string;
}

export interface AppInfoResult {
  /**
   * Whether app info retrieval was successful
   */
  success: boolean;

  /**
   * Package name of the app
   */
  packageName?: string;

  /**
   * Version name (e.g., "1.0.0")
   */
  versionName?: string;

  /**
   * Version code (numeric)
   */
  versionCode?: number;

  /**
   * Error message if retrieval failed
   */
  error?: string;
}

export interface UpdateRequiredResult {
  /**
   * Whether an update is required
   */
  updateRequired: boolean;

  /**
   * Current version that was compared
   */
  currentVersion: string;

  /**
   * Available version that was compared
   */
  availableVersion: string;
}

export interface DownloadProgressEvent {
  /**
   * Download progress percentage (0-100)
   */
  progress: number;

  /**
   * Bytes downloaded so far
   */
  bytesDownloaded: number;

  /**
   * Total bytes to download
   */
  totalSize: number;
}

export interface DownloadCompleteEvent {
  /**
   * Whether the download was successful
   */
  success: boolean;

  /**
   * Path to the downloaded file (if successful)
   */
  filePath?: string;

  /**
   * Whether the APK was automatically installed
   */
  installed?: boolean;

  /**
   * Error message if download failed
   */
  error?: string;
}
