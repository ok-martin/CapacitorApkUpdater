import { WebPlugin } from '@capacitor/core';

import type {
  CapacitorApkUpdaterPlugin,
  DownloadApkOptions,
  DownloadApkResult,
  DownloadStatusResult,
  CanInstallResult,
  PermissionResult,
  CancelDownloadResult,
  InstallApkResult,
  AppInfoResult,
  PluginListenerHandle,
} from './definitions';

export class CapacitorApkUpdaterWeb extends WebPlugin implements CapacitorApkUpdaterPlugin {
  async startApkDownload(_options: DownloadApkOptions): Promise<DownloadApkResult> {
    console.error('APK installation is not supported on web platform');
    return {
      success: false,
      error: 'APK installation is not supported on web platform',
    };
  }

  async getDownloadStatus(): Promise<DownloadStatusResult> {
    console.error('APK installation is not supported on web platform');
    return {
      success: false,
      bytesDownloaded: 0,
      totalSize: 0,
      progress: 0,
      status: -1,
    };
  }

  async canInstallApks(): Promise<CanInstallResult> {
    console.error('APK installation is not supported on web platform');
    return {
      canInstall: false,
      reason: 'APK installation is not supported on web platform',
    };
  }

  async requestInstallPermission(): Promise<PermissionResult> {
    console.error('APK installation is not supported on web platform');
    return {
      granted: false,
    };
  }

  async cancelDownload(): Promise<CancelDownloadResult> {
    console.error('APK installation is not supported on web platform');
    return {
      cancelled: false,
    };
  }

  async installApk(_options: { filePath: string }): Promise<InstallApkResult> {
    console.error('APK installation is not supported on web platform');
    return {
      success: false,
      error: 'APK installation is not supported on web platform',
    };
  }

  async getAppInfo(): Promise<AppInfoResult> {
    console.error('APK installation is not supported on web platform');
    return {
      success: false,
      error: 'App info is not available on web platform',
    };
  }

  async addListener(_: any, __: (event: any) => void): Promise<PluginListenerHandle> {
    console.error('APK installation is not supported on web platform');
    console.warn(`Event listener is not supported on web platform`);
    return {
      remove: async () => {
        // No-op for web
      },
    };
  }

  async removeAllListeners(): Promise<void> {
    console.error('APK installation is not supported on web platform');
  }
}
