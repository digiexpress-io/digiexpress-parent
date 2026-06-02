import { Fs, useFsDirent } from "@dxs-ts/fs-api";
import { FsTab, useFsNav } from '@dxs-ts/fs-nav';
import { useFsTheme } from '../fs-theme';
import { FsBreadcrumbProps } from "./FsBreadcrumbProps";


function _getAssetName(activeTab: FsTab | undefined, getDirentName: (id: string) => string | undefined): string | undefined {
  if (!activeTab) {
    return undefined;
  }
  if (activeTab.type === 'create') {
    return activeTab.direntType;
  }
  return getDirentName(activeTab.dirent.id) ?? activeTab.dirent.name;
}

export interface OwnerState {
  isDarkMode: boolean;
  assetName: string | undefined;
  assetPath: string | undefined;
  assetDirent: Fs.DirentBase | undefined;
  isError: boolean | undefined;
}

export function useOwnerState(_props: FsBreadcrumbProps): OwnerState {
  const { isDarkMode } = useFsTheme();
  const { openTabs, activeTabIndex, activeTabPath, activeDirent } = useFsNav();
  const { getDirent, getDirentName } = useFsDirent();

  const activeTab = openTabs[activeTabIndex];
  const assetName = _getAssetName(activeTab, getDirentName);
  const assetPath = activeTabPath;
  const activeDirentEntry = activeDirent ? getDirent(activeDirent.id) : undefined;
  const isError = (activeDirentEntry?.props?.errors.length ?? 0) > 0;

  return {
    assetDirent: activeDirent,
    assetName,
    assetPath,
    isError,
    isDarkMode
  }
}