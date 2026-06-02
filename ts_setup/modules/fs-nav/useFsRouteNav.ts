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

  const openTabs: FsTab[] = (search.openTabs as FsTabDescriptor[])
    .map((d: FsTabDescriptor) => toFsTab(d, getDirent))
    .filter((t): t is FsTab => t !== undefined);

  const activeTabIndex = search.activeTab !== undefined
    ? search.openTabs.findIndex((t: FsTabDescriptor) => toTabId(t) === search.activeTab)
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
    updateSearch(prev => mergeFsSearchParams({ type: 'create', direntType }, prev));
  }

  function closeTab(index: number) {
    const descriptor = search.openTabs[index];
    if (!descriptor) return;
    updateSearch(prev => closeFsTab(toTabId(descriptor), prev));
  }

  function closeAllTabs() {
    updateSearch(() => closeAllFsTabs());
  }

  function closeTabsToTheRight(index: number) {
    const descriptor = search.openTabs[index];
    if (!descriptor) return;
    updateSearch(prev => closeTabsToTheRightFn(toTabId(descriptor), prev));
  }

  function closeOtherTabs(index: number) {
    const descriptor = search.openTabs[index];
    if (!descriptor) {
      return;
    }
    updateSearch(prev => closeOtherFsTabs(toTabId(descriptor), prev));
  }

  function setActiveTab(index: number) {
    const descriptor = search.openTabs[index];
    if (!descriptor) return;
    updateSearch(prev => ({ ...prev, activeTab: toTabId(descriptor) }));
  }

  return {
    openTabs,
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
