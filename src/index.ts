import { registerPlugin } from '@capacitor/core';

import type { CapacitorApkUpdaterPlugin } from './definitions';

const CapacitorApkUpdater = registerPlugin<CapacitorApkUpdaterPlugin>('CapacitorApkUpdater', {
  web: () => import('./web').then((m) => new m.CapacitorApkUpdaterWeb()),
});

export * from './definitions';
export { CapacitorApkUpdater };
