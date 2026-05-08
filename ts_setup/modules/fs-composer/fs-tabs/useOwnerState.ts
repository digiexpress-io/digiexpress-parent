import React from "react";
import { useIntl } from "react-intl";
import { useFsNav, useFsDirent } from "@dxs-ts/fs-api";
import { useFsRouteNav } from '@dxs-ts/fs-nav';
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
  const intl = useIntl();
  const { isDarkMode } = useFsNav();
  const { openTabs, activeTabIndex, setActiveTab, closeTab } = useFsRouteNav();
  const { getDirent, getArticleName } = useFsDirent();

  const onTabClick = (index: number) => {
    setActiveTab(index);
  };

  const onTabClose = (index: number, event: React.MouseEvent) => {
    event.stopPropagation();
    closeTab(index);
  };

  const tabs: OwnerState['tabs'] = openTabs.map((tab, index) => ({
    id: tab.type === 'edit' ? tab.dirent.id : `__create__${tab.direntType.toLocaleLowerCase()}`,
    name: tab.type === 'edit' ? (getArticleName(tab.dirent.id) ?? tab.dirent.name) : intl.formatMessage({ id: `fs.tabs.new.${tab.direntType.toLocaleLowerCase()}` }),
    isActive: activeTabIndex === index,
    isFirst: index === 0,
    isLast: index === openTabs.length - 1,
    isError: tab.type === 'edit' ? (getDirent(tab.dirent.id)?.props?.errors.length ?? 0) > 0 : false,
  }));


  return {
    isDarkMode,
    tabs,
    activeTabIndex,
    onTabClose,
    onTabClick
  }
}