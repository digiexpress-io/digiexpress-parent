import { useNavigate, useSearch } from '@tanstack/react-router';
import { FsTab, FsEditTab, FsCreateTab } from './fs-nav-types';
import { Fs, useFsDirent } from '../fs-api';

import {
  FsRouteSearchParams,
  FsTabDescriptor,
  mergeFsSearchParams,
  closeFsTab,
  closeAllFsTabs,
  closeTabsToTheRight as closeTabsToTheRightFn,
  closeOtherFsTabs,
  toTabId,
} from './fs-nav-types';

function toFsTab(descriptor: FsTabDescriptor, getDirent: (id: string) => Fs.DirentBase | undefined): FsTab | undefined {
  if (descriptor.type === 'edit') {
    const dirent = getDirent(descriptor.id);
    if (!dirent) return undefined;
    const tab: FsEditTab = { type: 'edit', dirent };
    return tab;
  }
  const tab: FsCreateTab = { type: 'create', direntType: descriptor.direntType, parentFolder: undefined };
  return tab;
}

function getTabPath(tab: FsTab, getDirentName: (id: string) => string | undefined): string {
  if (tab.type === 'create') {
    return tab.parentFolder?.fullPath ?? '';
  }
  if (tab.dirent.type === 'ARTICLE') {
    return tab.dirent.fullPath.split('/').slice(0, -1).join('/');
  }
  if (tab.dirent.type === 'PRINTOUT_PAGE') {
    const localeName = getDirentName(tab.dirent.id) ?? tab.dirent.name;
    return tab.dirent.fullPath.split('/').slice(0, -1).join('/') + '/' + localeName;
  }
  return tab.dirent.fullPath;
}

export function useFsRouteNav() {
  const search = useSearch({ from: '/secured/$locale/worker/filesystem/' });
  const navigate = useNavigate();
  const { getDirent, getDirentName } = useFsDirent();

  const openTabIds: string[] = [];
  const openTabs: FsTab[] = [];
  (search.openTabs as FsTabDescriptor[]).forEach((d: FsTabDescriptor) => {
    const tab = toFsTab(d, getDirent);
    if (tab !== undefined) {
      openTabs.push(tab);
      openTabIds.push(toTabId(d));
    }
  });

  const activeTabIndex = search.activeTab !== undefined
    ? Math.min(
        openTabIds.findIndex((id: string) => id === search.activeTab),
        openTabs.length - 1
      )
    : -1;

  const activeTab = activeTabIndex >= 0 ? openTabs[activeTabIndex] : undefined;
  const activeDirent: Fs.DirentBase | undefined = activeTab?.type === 'edit' ? activeTab.dirent : undefined;
  const activeTabPath: string = activeTab ? getTabPath(activeTab, getDirentName) : '';

  function updateSearch(updater: (prev: FsRouteSearchParams) => FsRouteSearchParams) {
    navigate({
      from: '/secured/$locale/worker/filesystem',
      to: '.',
      search: updater,
    });
  }

  function openAsset(asset: Fs.DirentBase) {
    updateSearch(prev => mergeFsSearchParams({ type: 'edit', id: asset.id }, prev));
  }

  function openCreateTab(direntType: Fs.BodyType, _parentFolder: Fs.DirentBase | undefined) {
    updateSearch(prev => mergeFsSearchParams({ type: 'create', direntType, instanceId: Date.now().toString(36) }, prev));
  }

  function closeTab(tabId: string) {
    updateSearch(prev => closeFsTab(tabId, prev));
  }

  function closeAllTabs() {
    updateSearch(() => closeAllFsTabs());
  }

  function closeTabsToTheRight(tabId: string) {
    updateSearch(prev => closeTabsToTheRightFn(tabId, prev));
  }

  function closeOtherTabs(tabId: string) {
    updateSearch(prev => closeOtherFsTabs(tabId, prev));
  }

  function setActiveTab(tabId: string) {
    updateSearch(prev => ({ ...prev, activeTab: tabId }));
  }

  return {
    openTabs,
    openTabIds,
    activeTabIndex,
    activeTabPath,
    activeDirent,
    openAsset,
    openCreateTab,
    closeTab,
    closeAllTabs,
    closeTabsToTheRight,
    closeOtherTabs,
    setActiveTab,
  };
}
