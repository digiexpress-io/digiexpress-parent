import { useNavigate, useSearch } from '@tanstack/react-router';
import { Fs, FsTab, FsEditTab, FsCreateTab, useFsDirent } from '@dxs-ts/fs-api';
import {
  FsRouteSearchParams,
  FsTabDescriptor,
  mergeFsSearchParams,
  closeFsTab,
  closeAllFsTabs,
  closeTabsToTheRight as closeTabsToTheRightFn,
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

function getTabPath(tab: FsTab): string {
  if (tab.type === 'create') {
    return tab.parentFolder?.fullPath ?? '';
  }
  if (tab.dirent.type === 'ARTICLE') {
    return tab.dirent.fullPath.split('/').slice(0, -1).join('/');
  }
  return tab.dirent.fullPath;
}

export function useFsRouteNav() {
  const search = useSearch({ from: '/secured/$locale/worker/filesystem/' });
  const navigate = useNavigate();
  const { getDirent } = useFsDirent();

  const openTabs: FsTab[] = search.openTabs
    .map(d => toFsTab(d, getDirent))
    .filter((t): t is FsTab => t !== undefined);

  const activeTabIndex = search.activeTab !== undefined
    ? search.openTabs.findIndex(t => toTabId(t) === search.activeTab)
    : -1;

  const activeTab = activeTabIndex >= 0 ? openTabs[activeTabIndex] : undefined;
  const activeDirent: Fs.DirentBase | undefined = activeTab?.type === 'edit' ? activeTab.dirent : undefined;
  const activeTabPath: string = activeTab ? getTabPath(activeTab) : '';

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
    setActiveTab,
  };
}
