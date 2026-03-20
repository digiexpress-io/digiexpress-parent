import { useFsNav, useFsDirentProps } from "@dxs-ts/fs-api";
import { FsTabProps } from "./FsTabProps";


export interface OwnerState {
  isDarkMode: boolean;
  tabs: {
    id: string;
    isActive: boolean;
    isFirst: boolean;
    isLast: boolean;
    isError: boolean;
    name: string;
  }[];
  activeTabIndex: number;
  onTabClose: (index: number, event: React.MouseEvent) => void;
  onTabClick: (index: number) => void;
}

export function useOwnerState(_props: FsTabProps): OwnerState {
  const { isDarkMode, openTabs, activeTabIndex, setActiveTab, closeTab } = useFsNav();
  const { getDirentProps } = useFsDirentProps();

  const onTabClick = (index: number) => {
    setActiveTab(index);
  };

  const onTabClose = (index: number, event: React.MouseEvent) => {
    event.stopPropagation();
    closeTab(index);
  };

  const tabs: OwnerState['tabs'] = openTabs.map((tab, index) => ({
    id: tab.type === 'edit' ? tab.dirent.id : `__create__${tab.direntType}`,
    name: tab.type === 'edit' ? tab.dirent.name : tab.direntType,
    isActive: activeTabIndex === index,
    isFirst: index === 0,
    isLast: index === openTabs.length - 1,
    isError: tab.type === 'edit' ? getDirentProps(tab.dirent.id).errors.length > 0 : false,
  }));


  return {
    isDarkMode,
    tabs,
    activeTabIndex,
    onTabClose,
    onTabClick
  }
}