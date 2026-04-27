import { Fs, FsTab, useFsNav, useFsDirent } from "@dxs-ts/fs-api";
import { FsBreadcrumbProps } from "./FsBreadcrumbProps";


export interface OwnerState {
  isDarkMode: boolean;
  assetName: string | undefined;
  assetPath: string | undefined;
  assetDirent: Fs.DirentBase | undefined;
  isError: boolean | undefined;
}

function getAssetName(activeTab: FsTab | undefined): string | undefined {
  if (!activeTab) {
    return undefined;
  }
  if (activeTab.type === 'create') {
    return activeTab.direntType;
  }
  return activeTab.dirent.name;
}

export function useOwnerState(_props: FsBreadcrumbProps): OwnerState {
  const { isDarkMode, openTabs, activeTabIndex, activeDirent } = useFsNav();
  const { getDirent } = useFsDirent();

  const activeTab = openTabs[activeTabIndex];
  const assetName = getAssetName(activeTab);
  const assetPath = activeTab?.type === 'create' ? activeTab.pathToTopParent : activeDirent?.fullPath;
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