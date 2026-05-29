import React, { Dispatch, SetStateAction } from 'react';
import { FsExplorerProps } from "./FsExplorerProps";
import { FilterData, filterTreeDirents, useFsSearch } from '../fs-search';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { useFsTheme } from '../fs-theme';

function collectParentIds(nodes: Fs.DirentBase[], acc: string[] = []): string[] {
  nodes.forEach(node => {
    if (node.children && node.children.length > 0) {
      acc.push(node.id);
      collectParentIds(node.children, acc);
    }
  });
  return acc;
}

function handleContextMenu(
  event: React.MouseEvent,
  dirent: Fs.DirentBase,
  setContextMenuData: React.Dispatch<React.SetStateAction<Fs.ContextMenuData | undefined>>,
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
  activeDirentId: string | undefined;
  openAsset: (asset: Fs.DirentBase) => void;

  filteredTreeData: Fs.DirentBase[];
  searchTerm: string;
  fsData: Fs.DirentBase[];
  filters: FilterData[];
  contextMenuData: Fs.ContextMenuData | undefined;

  setContextMenuData: Dispatch<SetStateAction<Fs.ContextMenuData | undefined>>;
  setContextMenuOpen: Dispatch<SetStateAction<boolean>>;
  onContextMenuClose: () => void;
  onContextMenu: (event: React.MouseEvent, dirent: Fs.DirentBase,
    setContextMenuData: React.Dispatch<React.SetStateAction<Fs.ContextMenuData | undefined>>,
    setContextMenuOpen: React.Dispatch<React.SetStateAction<boolean>>) => void;

  setIsDarkMode: (darkMode: boolean) => void;
  setSearchExpanded: (expanded: boolean) => void;
  setSearchTerm: (term: string) => void;
  setFilters: (filters: FilterData[]) => void;
  onDoubleClick: (dirent: Fs.DirentBase) => void;
  collapseAll: () => void;
  toggleDirent: (direntId: string) => void;
}

export const useOwnerState = (_props: FsExplorerProps): OwnerState => {
  const { isDarkMode, setIsDarkMode } = useFsTheme();
  const { openAsset, activeDirent } = useFsNav();
  const { getDirent, dirents, getExtension } = useFsDirent();
  const { collapseAll, setExpanded, setExpandedBatch, isExpanded } = useFsNav();
  const { search } = useFsSearch();

  const [contextMenuOpen, setContextMenuOpen] = React.useState(false);
  const [contextMenuData, setContextMenuData] = React.useState<Fs.ContextMenuData | undefined>();

  const filteredTreeData = React.useMemo(() => {
    return filterTreeDirents(dirents, search.searchTerm, search.activeFilters, getDirent, getExtension);
  }, [dirents, search.searchTerm, search.activeFilters, getDirent]);

  React.useEffect(() => {
    const hasSearchTerm = !!search.searchTerm.trim();
    const hasFilters = search.activeFilters.length > 0;
    if (hasSearchTerm || hasFilters) {
      setExpandedBatch(collectParentIds(filteredTreeData), true);
    }
  }, [filteredTreeData]);

  function handleSetSearchTerm(term: string) {
    search.setSearchTerm(term);
  }

  function handleSetFilters(newFilters: FilterData[]) {
    search.setActiveFilters(newFilters);
  }

  function onContextMenuClose() {
    setContextMenuOpen(false);
  }

  function onDoubleClick(dirent: Fs.DirentBase) {
    const fullDirent = getDirent(dirent.id);
    if (fullDirent) {
      openAsset(fullDirent);
    }
  }

  const isAnyDirentExpanded = dirents.some(dirent =>
    isExpanded(dirent.id) ||
    (dirent.children && dirent.children.some(child => isExpanded(child.id)))
  );

  return {
    isAnyDirentExpanded,
    isDarkMode,
    activeDirentId: activeDirent?.id,
    openAsset,
    isSearchExpanded: search.open,
    isContextMenuOpen: contextMenuOpen,

    filteredTreeData,
    searchTerm: search.searchTerm,
    fsData: dirents,
    filters: search.activeFilters,
    contextMenuData,

    onContextMenu: handleContextMenu,
    onDoubleClick,
    onContextMenuClose,

    setContextMenuOpen,
    setSearchExpanded: search.setOpen,
    setIsDarkMode,
    setSearchTerm: handleSetSearchTerm,
    setContextMenuData,
    setFilters: handleSetFilters,


    collapseAll,
    toggleDirent: (direntId: string) => {

      const current = isExpanded(direntId);
      setExpanded(direntId, !current);
    },
  }

}