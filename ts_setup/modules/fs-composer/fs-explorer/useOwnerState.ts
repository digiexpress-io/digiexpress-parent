import React, { Dispatch, SetStateAction } from 'react';
import { FsExplorerProps } from "./FsExplorerProps";
import { FilterData, filterTreeDirents } from '../fs-search';
import { Fs, useFsNav, useFsDirent } from '@dxs-ts/fs-api';

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
  setFsData: Dispatch<SetStateAction<Fs.DirentBase[]>>;
  setFilters: (filters: FilterData[]) => void;
  onDoubleClick: (dirent: Fs.DirentBase, pathToTopParent: string) => void;
  collapseAll: () => void;
  toggleDirent: (direntId: string) => void;
}

export const useOwnerState = (_props: FsExplorerProps): OwnerState => {
  const { isDarkMode, setIsDarkMode, openAsset, searchExpanded, setSearchExpanded } = useFsNav();
  const { getDirent, dirents, collapseAll, setExpanded, setExpandedBatch } = useFsDirent();
  const [fsData, setFsData] = React.useState<Fs.DirentBase[]>(dirents);
  const [contextMenuOpen, setContextMenuOpen] = React.useState(false);
  const [contextMenuData, setContextMenuData] = React.useState<Fs.ContextMenuData | undefined>();
  const [searchTerm, setSearchTerm] = React.useState('');
  const debounceRef = React.useRef<ReturnType<typeof setTimeout> | undefined>(undefined);
  const [filters, setFilters] = React.useState<FilterData[]>([]);

  const filteredTreeData = React.useMemo(() => {
    return filterTreeDirents(fsData, searchTerm, filters, getDirent)
  }, [fsData, searchTerm, filters, getDirent])

  function collectParentIds(nodes: Fs.DirentBase[], acc: string[] = []): string[] {
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

  function onDoubleClick(dirent: Fs.DirentBase, pathToTopParent: string) {
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