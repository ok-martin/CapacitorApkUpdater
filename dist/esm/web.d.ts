import { WebPlugin } from '@capacitor/core';
import type { CapacitorApkUpdaterPlugin, DownloadApkOptions, DownloadApkResult, DownloadStatusResult, CanInstallResult, PermissionResult, CancelDownloadResult, InstallApkResult, AppInfoResult, PluginListenerHandle } from './definitions';
export declare class CapacitorApkUpdaterWeb extends WebPlugin implements CapacitorApkUpdaterPlugin {
    startApkDownload(_options: DownloadApkOptions): Promise<DownloadApkResult>;
    getDownloadStatus(): Promise<DownloadStatusResult>;
    canInstallApks(): Promise<CanInstallResult>;
    requestInstallPermission(): Promise<PermissionResult>;
    cancelDownload(): Promise<CancelDownloadResult>;
    installApk(_options: {
        filePath: string;
    }): Promise<InstallApkResult>;
    getAppInfo(): Promise<AppInfoResult>;
    addListener(_: any, __: (event: any) => void): Promise<PluginListenerHandle>;
    removeAllListeners(): Promise<void>;
}
