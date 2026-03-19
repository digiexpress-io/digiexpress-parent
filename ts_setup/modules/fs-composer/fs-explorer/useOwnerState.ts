import React, { Dispatch, SetStateAction } from 'react';
import { FsExplorerProps } from "./FsExplorerProps";
import { FilterData, filterTreeDirents } from '../fs-search';
import { FsDirentContextMenuData, FsDirent, mockFsData, useFsNav, handleContextMenu, collapseAll, toggleDirent } from '@dxs-ts/fs-api';


export interface OwnerState {
  isDarkMode: boolean;
  isSearchExpanded: boolean;
  isAnyDirentExpanded: boolean;
  isContextMenuOpen: boolean;

  filteredTreeData: FsDirent[];
  searchTerm: string;
  fsData: FsDirent[];
  filters: FilterData[];
  contextMenuData: FsDirentContextMenuData | undefined;

  setContextMenuData: Dispatch<SetStateAction<FsDirentContextMenuData | undefined>>;
  setContextMenuOpen: Dispatch<SetStateAction<boolean>>;
  onContextMenuClose: () => void;
  onContextMenu: (event: React.MouseEvent, dirent: FsDirent,
    setContextMenuData: React.Dispatch<React.SetStateAction<FsDirentContextMenuData | undefined>>,
    setContextMenuOpen: React.Dispatch<React.SetStateAction<boolean>>) => void;

  setIsDarkMode: (darkMode: boolean) => void;
  setSearchExpanded: (expanded: boolean) => void;
  setSearchTerm: (searchTerm: string) => void;
  setFsData: Dispatch<SetStateAction<FsDirent[]>>;
  setFilters: Dispatch<SetStateAction<FilterData[]>>;
  onDoubleClick: (dirent: FsDirent, pathToTopParent: string) => void;
  collapseAll: (fsData: FsDirent[], setFsData: Dispatch<SetStateAction<FsDirent[]>>) => void;
  toggleDirent: (direntId: string, fsData: FsDirent[], setFsData: Dispatch<SetStateAction<FsDirent[]>>) => void;
}

export const useOwnerState = (_props: FsExplorerProps): OwnerState => {
  const { isDarkMode, setIsDarkMode, openAsset, searchExpanded, setSearchExpanded } = useFsNav();
  const [fsData, setFsData] = React.useState<FsDirent[]>(mockFsData);
  const [contextMenuOpen, setContextMenuOpen] = React.useState(false);
  const [contextMenuData, setContextMenuData] = React.useState<FsDirentContextMenuData | undefined>();
  const [searchTerm, setSearchTerm] = React.useState('');
  const [filters, setFilters] = React.useState<FilterData[]>([]);

  const filteredTreeData = React.useMemo(() => {
    return filterTreeDirents(fsData, searchTerm, filters)
  }, [fsData, searchTerm, filters])

  function onContextMenuClose() {
    setContextMenuOpen(false);
  }

  function onDoubleClick(dirent: FsDirent, pathToTopParent: string) {
    openAsset(dirent, pathToTopParent);
  }

  const isAnyDirentExpanded = fsData.some(dirent => dirent.expanded || (dirent.children && dirent.children.some(child => child.expanded)));

  return {
    isAnyDirentExpanded,
    isDarkMode,
    isSearchExpanded: searchExpanded,
    isContextMenuOpen: contextMenuOpen,

    filteredTreeData,
    searchTerm,
    fsData,
    filters,
    contextMenuData,

    onContextMenu: handleContextMenu,
    onDoubleClick,
    onContextMenuClose,

    setContextMenuOpen,
    setSearchExpanded,
    setIsDarkMode,
    setSearchTerm,
    setContextMenuData,
    setFsData,
    setFilters,

    collapseAll,
    toggleDirent,
  }

}