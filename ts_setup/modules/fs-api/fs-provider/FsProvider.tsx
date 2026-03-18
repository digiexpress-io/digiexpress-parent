import React from 'react';
import { FsNode } from '../fs-types';
import { mockFsData } from '../mock-fs-data';

export interface FsOpenTab {
  node: FsNode;
  pathToTopParent: string;
}

export interface ItemReferencesEntry {
  assetName: string;
  location: string;
}

export interface FsContextType {
  isDarkMode: boolean;
  searchExpanded: boolean;
  openTabs: FsOpenTab[];
  activeTabIndex: number;
  activeTabPath: string;
  activeNode: FsNode | undefined;
  isChildError: (node: FsNode) => boolean;
  findReferencesToNode: (node: FsNode) => ItemReferencesEntry[];
  openAsset: (asset: FsNode, pathToTopParent: string) => void;
  closeTab: (index: number) => void;
  setActiveTab: (index: number) => void;
  setIsDarkMode: (isDarkMode: boolean) => void;
  setSearchExpanded: (expanded: boolean) => void;
}

const FsContext = React.createContext<FsContextType | undefined>(undefined);

export interface FsProviderProps {
  children: React.ReactNode;
}

export const FsProvider: React.FC<FsProviderProps> = (props) => {
  const [isDarkMode, setIsDarkMode] = React.useState(false);
  const [searchExpanded, setSearchExpanded] = React.useState(false);
  const [openTabs, setOpenTabs] = React.useState<FsOpenTab[]>([]);
  const [activeTabIndex, setActiveTabIndex] = React.useState(0);
  const [activeTabPath, setActiveTabPath] = React.useState('');
  const activeTab = openTabs[activeTabIndex];
  const activeNode = activeTab?.node;



  const openAsset = React.useCallback((asset: FsNode, pathToTopParent: string) => {
    if (asset.type === 'folder') {
      return;
    }

    setActiveTabPath(pathToTopParent);

    setOpenTabs(prevTabs => {
      const existingIndex = prevTabs.findIndex(tab => tab.node.id === asset.id);
      if (existingIndex !== -1) {
        setActiveTabIndex(existingIndex);
        setActiveTabPath(prevTabs[existingIndex].pathToTopParent);
        return prevTabs;
      }

      const newTab: FsOpenTab = {
        node: asset,
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

  const setActiveTab = React.useCallback((index: number) => {
    setActiveTabIndex(index);
    setOpenTabs(currentTabs => {
      if (currentTabs[index]) {
        setActiveTabPath(currentTabs[index].pathToTopParent);
      }
      return currentTabs;
    });
  }, []);

  const isChildError = React.useCallback((node: FsNode): boolean => {
    if (node.errors && node.errors.length > 0) {
      return true;
    }
    if (node.children) {
      return node.children.some(child => isChildError(child));
    }
    return false;
  }, []);

  const findReferencesToNode = React.useCallback((targetNode: FsNode): ItemReferencesEntry[] => {
    const references: ItemReferencesEntry[] = [];

    function searchInNode(node: FsNode, path: string[] = []): void {
      const currentPath = [...path, node.name];

      // Check if this node is a reference to our target node
      if (node.reference && node.name === targetNode.name && node.id !== targetNode.id) {
        references.push({
          assetName: node.name,
          location: currentPath.slice(0, -1).join(' / ')
        });
      }

      // Recursively search children
      if (node.children) {
        node.children.forEach(child => searchInNode(child, currentPath));
      }
    }

    // Search through all mock data
    mockFsData.forEach(rootNode => searchInNode(rootNode));

    return references;
  }, []);

  const contextValue: FsContextType = React.useMemo(() => {
    return {
      isDarkMode,
      setIsDarkMode,
      searchExpanded,
      setSearchExpanded,
      isChildError,
      findReferencesToNode,
      openTabs,
      activeTabIndex,
      activeTabPath,
      activeNode,
      openAsset,
      closeTab,
      setActiveTab,

    };
  }, [isDarkMode, openTabs, activeTabIndex, activeTabPath, openAsset, closeTab, setActiveTab, activeNode, isChildError, findReferencesToNode, searchExpanded]);

  return (
    <FsContext.Provider value={contextValue}>
      {props.children}
    </FsContext.Provider>
  );
};

export function useFs(): FsContextType {
  const result = React.useContext(FsContext);
  if (!result) {
    throw new Error('FsContext is not created!');
  }
  return result;
}