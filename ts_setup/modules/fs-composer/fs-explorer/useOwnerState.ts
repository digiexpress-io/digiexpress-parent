import React, { Dispatch, SetStateAction } from 'react';
import { FsExplorerProps } from "./FsExplorerProps";
import { FilterData, filterTreeDirents } from '../fs-search';
import { FsDirent, useFsNav, useFsDirent } from '@dxs-ts/fs-api';

function handleContextMenu(
  event: React.MouseEvent,
  dirent: FsDirent.Dirent,
  setContextMenuData: React.Dispatch<React.SetStateAction<FsDirent.ContextMenuData | undefined>>,
  setContextMenuOpen: React.Dispatch<React.SetStateAction<boolean>>
) {
  event.preventDefault();
  setContextMenuData({ dirent, anchorPosition: { top: event.clientY, left: event.clientX } });
  setContextMenuOpen(true);
}


export interface OwnerState {
  isDarkMode: boolean;
  isSearchExpanded: boolean;
  isAnyDirentExpanded: boolean;
  isContextMenuOpen: boolean;

  filteredTreeData: FsDirent.Dirent[];
  searchTerm: string;
  fsData: FsDirent.Dirent[];
  filters: FilterData[];
  contextMenuData: FsDirent.ContextMenuData | undefined;

  setContextMenuData: Dispatch<SetStateAction<FsDirent.ContextMenuData | undefined>>;
  setContextMenuOpen: Dispatch<SetStateAction<boolean>>;
  onContextMenuClose: () => void;
  onContextMenu: (event: React.MouseEvent, dirent: FsDirent.Dirent,
    setContextMenuData: React.Dispatch<React.SetStateAction<FsDirent.ContextMenuData | undefined>>,
    setContextMenuOpen: React.Dispatch<React.SetStateAction<boolean>>) => void;

  setIsDarkMode: (darkMode: boolean) => void;
  setSearchExpanded: (expanded: boolean) => void;
  setSearchTerm: (term: string) => void;
  setFsData: Dispatch<SetStateAction<FsDirent.Dirent[]>>;
  setFilters: (filters: FilterData[]) => void;
  onDoubleClick: (dirent: FsDirent.Dirent, pathToTopParent: string) => void;
  collapseAll: () => void;
  toggleDirent: (direntId: string) => void;
}

export const useOwnerState = (_props: FsExplorerProps): OwnerState => {
  const { isDarkMode, setIsDarkMode, openAsset, searchExpanded, setSearchExpanded } = useFsNav();
  const { getDirent, dirents, collapseAll, setExpanded, setExpandedBatch } = useFsDirent();
  const [fsData, setFsData] = React.useState<FsDirent.Dirent[]>(dirents);
  const [contextMenuOpen, setContextMenuOpen] = React.useState(false);
  const [contextMenuData, setContextMenuData] = React.useState<FsDirent.ContextMenuData | undefined>();
  const [searchTerm, setSearchTerm] = React.useState('');
  const debounceRef = React.useRef<ReturnType<typeof setTimeout> | undefined>(undefined);
  const [filters, setFilters] = React.useState<FilterData[]>([]);

  const filteredTreeData = React.useMemo(() => {
    return filterTreeDirents(fsData, searchTerm, filters, getDirent)
  }, [fsData, searchTerm, filters, getDirent])

  function collectParentIds(nodes: FsDirent.Dirent[], acc: string[] = []): string[] {
    nodes.forEach(node => {
      if (node.children && node.children.length > 0) {
        acc.push(node.id);
        collectParentIds(node.children, acc);
      }
    });
    return acc;
  }

  function handleSetSearchTerm(term: string) {
    setSearchTerm(term);
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }
    debounceRef.current = setTimeout(() => {
      if (term) {
        const filtered = filterTreeDirents(fsData, term, filters, getDirent);
        setExpandedBatch(collectParentIds(filtered), true);
      }
    }, 350);
  }

  function handleSetFilters(newFilters: FilterData[]) {
    setFilters(newFilters);
    if (newFilters.length > 0) {
      const filtered = filterTreeDirents(fsData, searchTerm, newFilters, getDirent);
      setExpandedBatch(collectParentIds(filtered), true);
    }
  }

  function onContextMenuClose() {
    setContextMenuOpen(false);
  }

  function onDoubleClick(dirent: FsDirent.Dirent, pathToTopParent: string) {
    openAsset(dirent, pathToTopParent);
  }

  const isAnyDirentExpanded = fsData.some(dirent =>
    getDirent(dirent.id)?.expanded ||
    (dirent.children && dirent.children.some(child => getDirent(child.id)?.expanded))
  );

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
    setSearchTerm: handleSetSearchTerm,
    setContextMenuData,
    setFsData,
    setFilters: handleSetFilters,

    collapseAll,
    toggleDirent: (direntId: string) => {
      const current = getDirent(direntId)?.expanded;
      setExpanded(direntId, !current);
    },
  }

}