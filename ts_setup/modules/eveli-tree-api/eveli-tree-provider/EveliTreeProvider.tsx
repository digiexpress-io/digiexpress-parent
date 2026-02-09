import React, { act } from 'react';
import { TreeNode } from '../tree-types';

export interface EveliTreeOpenTab {
  node: TreeNode;
  pathToTopParent: string;
}

export interface EveliTreeContextType {
  isDarkMode: boolean;
  openTabs: EveliTreeOpenTab[];
  activeTabIndex: number;
  activeTabPath: string;
  activeNode: TreeNode | undefined;
  openAsset: (asset: TreeNode, pathToTopParent: string) => void;
  closeTab: (index: number) => void;
  setActiveTab: (index: number) => void;
  setIsDarkMode: (isDarkMode: boolean) => void;
}

const EveliTreeContext = React.createContext<EveliTreeContextType | undefined>(undefined);

export interface EveliTreeProviderProps {
  children: React.ReactNode;
}

export const EveliTreeProvider: React.FC<EveliTreeProviderProps> = (props) => {
  const [isDarkMode, setIsDarkMode] = React.useState(false);
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
        pathToTopParent
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

  const contextValue: EveliTreeContextType = React.useMemo(() => {
    return {
      isDarkMode,
      openTabs,
      activeTabIndex,
      activeTabPath,
      activeNode,
      openAsset,
      closeTab,
      setActiveTab,
      setIsDarkMode,
    };
  }, [isDarkMode, openTabs, activeTabIndex, activeTabPath, openAsset, closeTab, setActiveTab, activeNode
  ]);

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