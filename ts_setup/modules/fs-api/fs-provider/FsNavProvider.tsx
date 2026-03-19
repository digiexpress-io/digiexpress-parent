import React from 'react';
import { FsDirent } from '../fs-types';
import { mockFsData } from '../mock-fs-data';

export interface FsOpenTab {
  dirent: FsDirent;
  pathToTopParent: string;
}

export interface ItemReferencesEntry {
  assetName: string;
  location: string;
}

export interface FsNavContextType {
  isDarkMode: boolean;
  searchExpanded: boolean;
  openTabs: FsOpenTab[];
  activeTabIndex: number;
  activeTabPath: string;
  activeDirent: FsDirent | undefined;
  isChildError: (dirent: FsDirent) => boolean;
  findReferencesToDirent: (dirent: FsDirent) => ItemReferencesEntry[];
  openAsset: (asset: FsDirent, pathToTopParent: string) => void;
  closeTab: (index: number) => void;
  closeAllTabs: () => void;
  closeTabsToTheRight: (index: number) => void;
  setActiveTab: (index: number) => void;
  setIsDarkMode: (isDarkMode: boolean) => void;
  setSearchExpanded: (expanded: boolean) => void;
}

const FsNavContext = React.createContext<FsNavContextType | undefined>(undefined);

export interface FsNavProviderProps {
  children: React.ReactNode;
}

export const FsNavProvider: React.FC<FsNavProviderProps> = (props) => {
  const [isDarkMode, setIsDarkMode] = React.useState(false);
  const [searchExpanded, setSearchExpanded] = React.useState(false);
  const [openTabs, setOpenTabs] = React.useState<FsOpenTab[]>([]);
  const [activeTabIndex, setActiveTabIndex] = React.useState(0);
  const [activeTabPath, setActiveTabPath] = React.useState('');
  const activeTab = openTabs[activeTabIndex];
  const activeDirent = activeTab?.dirent;



  const openAsset = React.useCallback((asset: FsDirent, pathToTopParent: string) => {
    if (asset.type === 'folder') {
      return;
    }

    setActiveTabPath(pathToTopParent);

    setOpenTabs(prevTabs => {
      const existingIndex = prevTabs.findIndex(tab => tab.dirent.id === asset.id);
      if (existingIndex !== -1) {
        setActiveTabIndex(existingIndex);
        setActiveTabPath(prevTabs[existingIndex].pathToTopParent);
        return prevTabs;
      }

      const newTab: FsOpenTab = {
        dirent: asset,
        pathToTopParent,
      };
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
        setActiveTabPath(currentTabs[index].pathToTopParent);
      }
      return currentTabs;
    });
  }, []);

  const isChildError = React.useCallback((dirent: FsDirent): boolean => {
    if (dirent.errors && dirent.errors.length > 0) {
      return true;
    }
    if (dirent.children) {
      return dirent.children.some(child => isChildError(child));
    }
    return false;
  }, []);

  const findReferencesToDirent = React.useCallback((targetDirent: FsDirent): ItemReferencesEntry[] => {
    const references: ItemReferencesEntry[] = [];

    function searchInDirent(dirent: FsDirent, path: string[] = []): void {
      const currentPath = [...path, dirent.name];

      // Check if this dirent is a reference to our target dirent
      if (dirent.reference && dirent.name === targetDirent.name && dirent.id !== targetDirent.id) {
        references.push({
          assetName: dirent.name,
          location: currentPath.slice(0, -1).join(' / ')
        });
      }

      // Recursively search children
      if (dirent.children) {
        dirent.children.forEach(child => searchInDirent(child, currentPath));
      }
    }

    // Search through all mock data
    mockFsData.forEach(rootDirent => searchInDirent(rootDirent));

    return references;
  }, []);

  const contextValue: FsNavContextType = React.useMemo(() => {
    return {
      isDarkMode,
      setIsDarkMode,
      searchExpanded,
      setSearchExpanded,
      isChildError,
      findReferencesToDirent,
      openTabs,
      activeTabIndex,
      activeTabPath,
      activeDirent,
      openAsset,
      closeTab,
      closeAllTabs,
      closeTabsToTheRight,
      setActiveTab,

    };
  }, [isDarkMode, openTabs, activeTabIndex, activeTabPath, openAsset, closeTab, closeAllTabs, closeTabsToTheRight, setActiveTab, activeDirent, isChildError, findReferencesToDirent, searchExpanded]);

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
