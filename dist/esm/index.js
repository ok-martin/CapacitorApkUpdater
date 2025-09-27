import { registerPlugin } from '@capacitor/core';
const CapacitorApkUpdater = registerPlugin('CapacitorApkUpdater', {
    web: () => import('./web').then((m) => new m.CapacitorApkUpdaterWeb()),
});
export * from './definitions';
export { CapacitorApkUpdater };
//# sourceMappingURL=index.js.map