import React from 'react';
import { TreeNode } from '../tree-types';
import { mockTreeData } from '../mock-tree-data';

export interface EveliTreeOpenTab {
  node: TreeNode;
  pathToTopParent: string;
}

export interface ItemReferencesEntry {
  assetName: string;
  location: string;
}

export interface EveliTreeContextType {
  isDarkMode: boolean;
  searchExpanded: boolean;
  openTabs: EveliTreeOpenTab[];
  activeTabIndex: number;
  activeTabPath: string;
  activeNode: TreeNode | undefined;
  isChildError: (node: TreeNode) => boolean;
  findReferencesToNode: (node: TreeNode) => ItemReferencesEntry[];
  openAsset: (asset: TreeNode, pathToTopParent: string) => void;
  closeTab: (index: number) => void;
  setActiveTab: (index: number) => void;
  setIsDarkMode: (isDarkMode: boolean) => void;
  setSearchExpanded: (expanded: boolean) => void;
}

const EveliTreeContext = React.createContext<EveliTreeContextType | undefined>(undefined);

export interface EveliTreeProviderProps {
  children: React.ReactNode;
}

export const EveliTreeProvider: React.FC<EveliTreeProviderProps> = (props) => {
  const [isDarkMode, setIsDarkMode] = React.useState(false);
  const [searchExpanded, setSearchExpanded] = React.useState(false);
  const [openTabs, setOpenTabs] = React.useState<EveliTreeOpenTab[]>([]);
  const [activeTabIndex, setActiveTabIndex] = React.useState(0);
  const [activeTabPath, setActiveTabPath] = React.useState('');
  const activeTab = openTabs[activeTabIndex];
  const activeNode = activeTab?.node;



  const openAsset = React.useCallback((asset: TreeNode, pathToTopParent: string) => {
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

      const newTab: EveliTreeOpenTab = {
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

  const isChildError = React.useCallback((node: TreeNode): boolean => {
    if (node.error) {
      return true;
    }
    if (node.children) {
      return node.children.some(child => isChildError(child));
    }
    return false;
  }, []);

  const findReferencesToNode = React.useCallback((targetNode: TreeNode): ItemReferencesEntry[] => {
    const references: ItemReferencesEntry[] = [];

    function searchInNode(node: TreeNode, path: string[] = []): void {
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
    mockTreeData.forEach(rootNode => searchInNode(rootNode));

    return references;
  }, []);

  const contextValue: EveliTreeContextType = React.useMemo(() => {
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
    <EveliTreeContext.Provider value={contextValue}>
      {props.children}
    </EveliTreeContext.Provider>
  );
};

export function useEveliTree(): EveliTreeContextType {
  const result = React.useContext(EveliTreeContext);
  if (!result) {
    throw new Error('EveliTreeContext is not created!');
  }
  return result;
}