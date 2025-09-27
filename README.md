# Capacitor APK Updater Plugin (capacitor-apk-updater)

A Capacitor plugin for downloading and installing APK updates on Android devices for when your app is not yet published in a store.

**This plugin is only for Android apps that are not published in the store.**

## Installation

```bash
npm install https://github.com/ok-martin/CapacitorApkUpdater
npx cap sync android
```

## Configuration for Android

1. Add the FileProvider to your app's `android/app/src/main/AndroidManifest.xml`:

```xml
<application>
    <!-- Other configuration -->

    <provider
        android:name="androidx.core.content.FileProvider"
        android:authorities="${applicationId}.fileprovider"
        android:exported="false"
        android:grantUriPermissions="true"
    >
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths" />
    </provider>

    <!-- Other configuration -->
</application>
```

2. Make sure your app has the following permissions in `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

3. Add `<external-files-path name="downloads" path="Download" />` to `android/app/src/main/res/xml/file_paths.xml`

Example `file_paths.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <!-- Other configuration -->
    <external-files-path name="downloads" path="Download" />
</paths>
```

4. Ensure these files contain the following code

`android/app/capacitor.build.gradle`:

```gradle
implementation project(':capacitor-apk-updater')
```

`/android/capacitor.settings.gradle`:

```gradle
include ':capacitor-apk-updater'
project(':capacitor-apk-updater').projectDir = new File('../node_modules/capacitor-apk-updater/android')
```

## Usage

### Basic Usage

```typescript
import { CapacitorApkUpdater } from 'capacitor-apk-updater';

// Check if the app can install APKs
const canInstall = await CapacitorApkUpdater.canInstallApks();
if (!canInstall.canInstall) {
  // Request permission if needed
  const permissionResult = await CapacitorApkUpdater.requestInstallPermission();
  if (!permissionResult.granted) {
    console.log('Permission denied');
    return;
  }
}

// Download and install APK
try {
  const result = await CapacitorApkUpdater.startApkDownload({
    url: 'https://example.com/path/to/your/app-update.apk',
    filename: 'app-update.apk',
    showNotification: true,
    notificationTitle: 'Downloading App Update...',
  });

  if (result.success) {
    console.log('Download started successfully');
  } else {
    console.error('Failed to start download:', result.error);
  }
} catch (error) {
  console.error('Error:', error);
}
```

### Real-time Progress Tracking

```typescript
import { CapacitorApkUpdater } from 'capacitor-apk-updater';

// Listen for download progress
const progressListener = await CapacitorApkUpdater.addListener('downloadProgress', (event) => {
  console.log(`Progress: ${event.progress}%`);
  console.log(`Downloaded: ${event.bytesDownloaded} / ${event.totalSize} bytes`);
});

// Listen for download completion
const completeListener = await CapacitorApkUpdater.addListener('downloadComplete', (event) => {
  if (event.success) {
    console.log('Download completed successfully');
    console.log('Auto-installation attempted:', event.installed);
  } else {
    console.error('Download failed:', event.error);
  }
});

// Start download
await CapacitorApkUpdater.startApkDownload({
  url: 'https://example.com/update.apk',
});

// Clean up listeners when done
progressListener.remove();
completeListener.remove();
```

## Android Download Manager Status Codes

The `getDownloadStatus()` method returns Android's DownloadManager status codes:

- `1` - STATUS_PENDING: Download is waiting to start
- `2` - STATUS_RUNNING: Download is in progress
- `4` - STATUS_PAUSED: Download is paused
- `8` - STATUS_SUCCESSFUL: Download completed successfully
- `16` - STATUS_FAILED: Download failed

## Rollup Configuration

Create a `rollup.config.js`:

```javascript
import nodeResolve from '@rollup/plugin-node-resolve';

export default {
  input: 'dist/esm/index.js',
  output: [
    {
      file: 'dist/plugin.js',
      format: 'iife',
      name: 'capacitorApkUpdater',
      globals: {
        '@capacitor/core': 'capacitorExports',
      },
      sourcemap: true,
      inlineDynamicImports: true,
    },
    {
      file: 'dist/plugin.cjs.js',
      format: 'cjs',
      sourcemap: true,
      inlineDynamicImports: true,
    },
  ],
  external: ['@capacitor/core'],
  plugins: [
    nodeResolve({
      browser: true,
    }),
  ],
};
```

## License

MIT
