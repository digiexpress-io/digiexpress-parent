import React from 'react';
import { useFs, FsNode } from '@dxs-ts/fs-api';
import { FsSearchProps } from './FsSearchProps';
import { FsNodeType } from '../fs-theme';

export interface FilterData {
  label: string;
  type: FsNodeType;
}

export function filterTreeNodes(
  nodes: FsNode[],
  searchTerm: string,
  visibleFilters: FilterData[]
): FsNode[] {
  const visibleTypes = visibleFilters.map(filter => filter.type);
  const isNoFiltersSelected = visibleFilters.length === 0;
  const isSearchTermEmpty = !searchTerm.trim() || searchTerm.trim().length < 3;

  if (isSearchTermEmpty && isNoFiltersSelected) {
    return nodes;
  }

  const filtered: FsNode[] = [];

  for (const node of nodes) {
    const nameMatches = node.name.toLowerCase().includes(searchTerm.toLowerCase());
    const descriptionMatches = node.description?.toLowerCase().includes(searchTerm.toLowerCase());
    const typeMatches = isNoFiltersSelected || visibleTypes.includes(node.type);
    const childMatches = node.children ? filterTreeNodes(node.children, searchTerm, visibleFilters) : [];

    const showBySearch = isSearchTermEmpty || nameMatches || descriptionMatches;

    if ((showBySearch && typeMatches) || childMatches.length > 0) {
      filtered.push({
        ...node,
        expanded: childMatches.length > 0 ? true : node.expanded,
        children: childMatches.length > 0 ? childMatches : node.children
      });
    }
  }

  return filtered;
}

const allAvailableFilters: FilterData[] = [
  { label: 'Articles', type: 'article' },
  { label: 'Dialobs', type: 'dialob' },
  { label: 'Services', type: 'service' },
  { label: 'Pages', type: 'folder' },
  { label: 'Links', type: 'link' },
  { label: 'Flows', type: 'flow' },
  { label: 'Printouts', type: 'printout' },
  { label: 'Images', type: 'image' }
];

export interface OwnerState {
  searchTerm: string;
  visibleFilters: FilterData[];
  open: boolean;
  onSearchChange: (value: string) => void;
  onFiltersChange: (filters: FilterData[]) => void;

  isDarkMode: boolean;
  allAvailableFilters: FilterData[];
  handleSearchChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
  handleFilterSelectChange: (selectedLabels: string[]) => void;
}

export const useOwnerState = (props: FsSearchProps): OwnerState => {
  const { isDarkMode } = useFs();

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    props.onSearchChange(event.target.value);
  };

  const handleFilterSelectChange = (selectedLabels: string[]) => {
    const selectedFilters = allAvailableFilters.filter(filter =>
      selectedLabels.includes(filter.label)
    );
    props.onFiltersChange(selectedFilters);
  };

  return {
    searchTerm: props.searchTerm,
    visibleFilters: props.visibleFilters,
    open: props.open,
    onSearchChange: props.onSearchChange,
    onFiltersChange: props.onFiltersChange,
    isDarkMode,
    allAvailableFilters,
    handleSearchChange,
    handleFilterSelectChange,
  };
};