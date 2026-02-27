import React, { Dispatch, SetStateAction } from 'react';
import { FsExplorerProps } from "./FsExplorerProps";
import { FilterData, filterTreeNodes } from '../fs-search';
import { FsContextMenuData, FsNode, mockFsData, useFs, handleContextMenu, collapseAll, toggleNode } from '@dxs-ts/fs-api';


export interface OwnerState {
isDarkMode: boolean;
searchExpanded: boolean;
isAnyNodeExpanded: boolean;
contextMenuOpen: boolean;

filteredTreeData: FsNode[];
searchTerm: string;
fsData: FsNode[];
filters: FilterData[];
contextMenuData: FsContextMenuData | undefined;

setContextMenuData: Dispatch<SetStateAction<FsContextMenuData | undefined>>;
setContextMenuOpen:Dispatch<SetStateAction<boolean>>;
onContextMenuClose:() => void;
onContextMenu: (event: React.MouseEvent, node: FsNode,
  setContextMenuData: React.Dispatch<React.SetStateAction<FsContextMenuData | undefined>>,
  setContextMenuOpen: React.Dispatch<React.SetStateAction<boolean>>) => void;

setIsDarkMode: (darkMode: boolean) => void;
setSearchExpanded: (expanded: boolean) => void;
setSearchTerm: (searchTerm: string) => void;
setFsData:Dispatch<SetStateAction<FsNode[]>>;
setFilters:Dispatch<SetStateAction<FilterData[]>>;
onDoubleClick:(node: FsNode, pathToTopParent: string) => void;
collapseAll: (fsData: FsNode[], setFsData: Dispatch<SetStateAction<FsNode[]>>) => void;
toggleNode: (nodeId: string, fsData: FsNode[], setFsData: Dispatch<SetStateAction<FsNode[]>>) => void;
}

export const useOwnerState = (_props: FsExplorerProps): OwnerState => {
   const { isDarkMode, setIsDarkMode, openAsset, searchExpanded, setSearchExpanded } = useFs();
  const [fsData, setFsData] = React.useState<FsNode[]>(mockFsData);
  const [contextMenuOpen, setContextMenuOpen] = React.useState(false);
  const [contextMenuData, setContextMenuData] = React.useState<FsContextMenuData | undefined>();
  const [searchTerm, setSearchTerm] = React.useState('');
  const [filters, setFilters] = React.useState<FilterData[]>([]);

  const filteredTreeData = React.useMemo(() => {
    return filterTreeNodes(fsData, searchTerm, filters)
  }, [fsData, searchTerm, filters])

  function onContextMenuClose() {
    setContextMenuOpen(false);
  }

  function onDoubleClick(node: FsNode, pathToTopParent: string) {
    openAsset(node, pathToTopParent);
  }

  const isAnyNodeExpanded = fsData.some(node => node.expanded || (node.children && node.children.some(child => child.expanded)));

  return {
    isAnyNodeExpanded,
    isDarkMode,
    searchExpanded,
    contextMenuOpen,

    filteredTreeData,
    searchTerm,
    fsData,
    filters,
    contextMenuData,

    onContextMenu: handleContextMenu,
    setContextMenuOpen,
    onContextMenuClose,
    setSearchExpanded,
    setIsDarkMode,
    setSearchTerm,
    setContextMenuData,
    setFsData,
    setFilters,
    onDoubleClick,
    collapseAll,
    toggleNode,
  }
  
}