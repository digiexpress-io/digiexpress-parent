import React from 'react';
import { Fs } from '../fs-types';

export interface FsEditTab {
  type: 'edit';
  dirent: Fs.DirentBase;
}

export interface FsCreateTab {
  type: 'create';
  direntType: Fs.BodyType;
  parentFolder: Fs.DirentBase | undefined;
}

export type FsTab = FsEditTab | FsCreateTab;

export interface FsNavContextType {
  isDarkMode: boolean;
  openTabs: FsTab[];
  activeTabIndex: number;
  activeTabPath: string;
  activeDirent: Fs.DirentBase | undefined;
  expandedIds: string[];
  isExpanded: (id: string) => boolean;
  openAsset: (asset: Fs.DirentBase) => void;
  openCreateTab: (direntType: Fs.BodyType, parentFolder: Fs.DirentBase | undefined) => void;
  closeTab: (index: number) => void;
  closeAllTabs: () => void;
  closeTabsToTheRight: (index: number) => void;
  setActiveTab: (index: number) => void;
  setIsDarkMode: (isDarkMode: boolean) => void;

  setExpanded: (id: string, value: boolean) => void;
  setExpandedBatch: (ids: string[], value: boolean) => void;
  collapseAll: () => void;
}

const FsNavContext = React.createContext<FsNavContextType | undefined>(undefined);

export interface FsNavProviderProps {
  children: React.ReactNode;
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

export const FsNavProvider: React.FC<FsNavProviderProps> = (props) => {
  const [isDarkMode, setIsDarkMode] = React.useState(false);
  const [openTabs, setOpenTabs] = React.useState<FsTab[]>([]);
  const [activeTabIndex, setActiveTabIndex] = React.useState(0);
  const [activeTabPath, setActiveTabPath] = React.useState('');
  const [expandedIds, setExpandedIds] = React.useState<string[]>([]);
  const activeTab = openTabs[activeTabIndex];
  const activeDirent = activeTab?.type === 'edit' ? activeTab.dirent : undefined;

  const setExpanded = React.useCallback((id: string, isExpanded: boolean) => {
    setExpandedIds(prev => {
      if (isExpanded) {
        return [...prev, id];
      }
      return prev.filter(i => i !== id);
    });
  }, []);

  const setExpandedBatch = React.useCallback((ids: string[], isExpanded: boolean) => {
    setExpandedIds(prev => {
      if (isExpanded) {
        return [...prev, ...ids];
      }
      return prev.filter(i => !ids.includes(i));
    });
  }, []);

  const collapseAll = React.useCallback(() => {
    setExpandedIds([]);
  }, []);

  const openAsset = React.useCallback((asset: Fs.DirentBase) => {
    setOpenTabs(prevTabs => {
      const existingIndex = prevTabs.findIndex(tab => tab.type === 'edit' && tab.dirent.id === asset.id);
      if (existingIndex !== -1) {
        setActiveTabIndex(existingIndex);
        setActiveTabPath(getTabPath(prevTabs[existingIndex]));
        return prevTabs;
      }

      const newTab: FsEditTab = {
        type: 'edit',
        dirent: asset,
      };
      const newTabs = [...prevTabs, newTab];
      setActiveTabIndex(newTabs.length - 1);
      setActiveTabPath(getTabPath(newTab));
      return newTabs;
    });
  }, []);

  const openCreateTab = React.useCallback((direntType: Fs.BodyType, parentFolder: Fs.DirentBase | undefined) => {
    const newTab: FsCreateTab = {
      type: 'create',
      direntType,
      parentFolder,
    };
    setActiveTabPath(getTabPath(newTab));
    setOpenTabs(prevTabs => {
      const newTabs = [...prevTabs, newTab];
      setActiveTabIndex(newTabs.length - 1);
      return newTabs;
    });
  }, []);

  const closeTab = React.useCallback((index: number) => {
    setOpenTabs(prevTabs => {
      const newTabs = prevTabs.filter((_, i) => i !== index);
      if (index <= activeTabIndex) {
        const newActiveIndex = Math.max(0, activeTabIndex - 1);
        setActiveTabIndex(newActiveIndex);
      }

      return newTabs;
    });
  }, [activeTabIndex]);

  const closeTabsToTheRight = React.useCallback((index: number) => {
    setOpenTabs(prevTabs => {
      const newTabs = prevTabs.slice(0, index + 1);
      if (activeTabIndex > index) {
        setActiveTabIndex(index);
      }
      return newTabs;
    });
  }, [activeTabIndex]);

  const closeAllTabs = React.useCallback(() => {
    setOpenTabs([]);
    setActiveTabIndex(0);
  }, []);

  const setActiveTab = React.useCallback((index: number) => {
    setActiveTabIndex(index);
    setOpenTabs(currentTabs => {
      if (currentTabs[index]) {
        setActiveTabPath(getTabPath(currentTabs[index]));
      }
      return currentTabs;
    });
  }, []);

  const contextValue: FsNavContextType = React.useMemo(() => {
    return {
      isExpanded: (id) => expandedIds.includes(id),
      isDarkMode,
      setIsDarkMode,
      openTabs,
      expandedIds,
      activeTabIndex,
      activeTabPath,
      activeDirent,
      openAsset,
      openCreateTab,
      closeTab,
      closeAllTabs,
      closeTabsToTheRight,
      setActiveTab,
      collapseAll,
      setExpanded,
      setExpandedBatch
    };
  }, [isDarkMode, openTabs, activeTabIndex, activeTabPath, expandedIds, setExpanded, collapseAll, setExpandedBatch, openAsset, openCreateTab, closeTab, closeAllTabs, closeTabsToTheRight, setActiveTab, activeDirent]);

  return (
    <FsNavContext.Provider value={contextValue}>
      {props.children}
    </FsNavContext.Provider>
  );
};

export function useFsNav(): FsNavContextType {
  const result = React.useContext(FsNavContext);
  if (!result) {
    throw new Error('FsNavContext is not created!');
  }
  return result;
}
