import { WebPlugin } from '@capacitor/core';
export class CapacitorApkUpdaterWeb extends WebPlugin {
    async startApkDownload(_options) {
        console.error('APK installation is not supported on web platform');
        return {
            success: false,
            error: 'APK installation is not supported on web platform',
        };
    }
    async getDownloadStatus() {
        console.error('APK installation is not supported on web platform');
        return {
            success: false,
            bytesDownloaded: 0,
            totalSize: 0,
            progress: 0,
            status: -1,
        };
    }
    async canInstallApks() {
        console.error('APK installation is not supported on web platform');
        return {
            canInstall: false,
            reason: 'APK installation is not supported on web platform',
        };
    }
    async requestInstallPermission() {
        console.error('APK installation is not supported on web platform');
        return {
            granted: false,
        };
    }
    async cancelDownload() {
        console.error('APK installation is not supported on web platform');
        return {
            cancelled: false,
        };
    }
    async installApk(_options) {
        console.error('APK installation is not supported on web platform');
        return {
            success: false,
            error: 'APK installation is not supported on web platform',
        };
    }
    async getAppInfo() {
        console.error('APK installation is not supported on web platform');
        return {
            success: false,
            error: 'App info is not available on web platform',
        };
    }
    async addListener(_, __) {
        console.error('APK installation is not supported on web platform');
        console.warn(`Event listener is not supported on web platform`);
        return {
            remove: async () => {
                // No-op for web
            },
        };
    }
    async removeAllListeners() {
        console.error('APK installation is not supported on web platform');
    }
}
//# sourceMappingURL=web.js.map