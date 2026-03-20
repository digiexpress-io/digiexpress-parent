import { FsDirent, FsTab, useFsNav, useFsDirentProps } from "@dxs-ts/fs-api";
import { FsBreadcrumbProps } from "./FsBreadcrumbProps";


export interface OwnerState {
  isDarkMode: boolean;
  assetName: string | undefined;
  assetPath: string | undefined;
  assetDirent: FsDirent | undefined;
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

function getAssetPath(activeTab: FsTab | undefined): string | undefined {
  if (!activeTab) {
    return undefined;
  }
  if (activeTab.type === 'create') {
    return activeTab.pathToTopParent;
  }
  return activeTab.pathToTopParent.split(' / ').slice(0, -1).join(' / ');
}

export function useOwnerState(_props: FsBreadcrumbProps): OwnerState {
  const { isDarkMode, openTabs, activeTabIndex, activeDirent } = useFsNav();
  const { getDirentProps } = useFsDirentProps();

  const activeTab = openTabs[activeTabIndex];
  const assetName = getAssetName(activeTab);
  const assetPath = getAssetPath(activeTab);

  const direntProps = getDirentProps(activeDirent?.id ?? '');
  const isError = direntProps.errors.length > 0;

  return {
    assetDirent: activeDirent,
    assetName,
    assetPath,
    isError,
    isDarkMode
  }
}