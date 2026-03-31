import React from 'react';
import { Fs } from '../fs-types';

export interface FsEditTab {
  type: 'edit';
  dirent: Fs.Dirent;
  pathToTopParent: string;
}

export interface FsCreateTab {
  type: 'create';
  direntType: Fs.Type;
  parentFolder: Fs.Dirent | undefined;
  pathToTopParent: string;
}

export type FsTab = FsEditTab | FsCreateTab;

export interface FsNavContextType {
  isDarkMode: boolean;
  searchExpanded: boolean;
  openTabs: FsTab[];
  activeTabIndex: number;
  activeTabPath: string;
  activeDirent: Fs.Dirent | undefined;
  openAsset: (asset: Fs.Dirent, pathToTopParent: string) => void;
  openCreateTab: (direntType: Fs.Type, parentFolder: Fs.Dirent | undefined) => void;
  registerDirentPath: (id: string, path: string) => void;
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
  const [openTabs, setOpenTabs] = React.useState<FsTab[]>([]);
  const [activeTabIndex, setActiveTabIndex] = React.useState(0);
  const [activeTabPath, setActiveTabPath] = React.useState('');
  const activeTab = openTabs[activeTabIndex];
  const activeDirent = activeTab?.type === 'edit' ? activeTab.dirent : undefined;

  const direntPathsRef = React.useRef<Record<string, string>>({});

  const registerDirentPath = React.useCallback((id: string, path: string) => {
    direntPathsRef.current[id] = path;
  }, []);

  const openAsset = React.useCallback((asset: Fs.Dirent, pathToTopParent: string) => {
    setActiveTabPath(pathToTopParent);

    setOpenTabs(prevTabs => {
      const existingIndex = prevTabs.findIndex(tab => tab.type === 'edit' && tab.dirent.id === asset.id);
      if (existingIndex !== -1) {
        setActiveTabIndex(existingIndex);
        setActiveTabPath(prevTabs[existingIndex].pathToTopParent);
        return prevTabs;
      }

      const newTab: FsEditTab = {
        type: 'edit',
        dirent: asset,
        pathToTopParent,
      };
      const newTabs = [...prevTabs, newTab];
      setActiveTabIndex(newTabs.length - 1);
      return newTabs;
    });
  }, []);

  const openCreateTab = React.useCallback((direntType: Fs.Type, parentFolder: Fs.Dirent | undefined) => {
    const registeredPath = parentFolder ? (direntPathsRef.current[parentFolder.id] ?? parentFolder.name) : '';

    const pathToTopParent = parentFolder?.type === 'folder' ? registeredPath : registeredPath
      .split(' / ')
      .slice(0, -1)
      .join(' / ');

    setActiveTabPath(pathToTopParent);

    setOpenTabs(prevTabs => {
      const newTab: FsCreateTab = {
        type: 'create',
        direntType,
        parentFolder,
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

  const contextValue: FsNavContextType = React.useMemo(() => {
    return {
      isDarkMode,
      setIsDarkMode,
      searchExpanded,
      setSearchExpanded,
      openTabs,
      activeTabIndex,
      activeTabPath,
      activeDirent,
      openAsset,
      openCreateTab,
      registerDirentPath,
      closeTab,
      closeAllTabs,
      closeTabsToTheRight,
      setActiveTab,
    };
  }, [isDarkMode, openTabs, activeTabIndex, activeTabPath, openAsset, openCreateTab, registerDirentPath, closeTab, closeAllTabs, closeTabsToTheRight, setActiveTab, activeDirent, searchExpanded]);

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
