import React from 'react';
import { useFsNav, Fs } from '@dxs-ts/fs-api';
import { FsSearchProps } from './FsSearchProps';

export interface FilterData {
  label: string;
  type: Fs.Type;
}

export function filterTreeDirents(
  dirents: Fs.Dirent[],
  searchTerm: string,
  visibleFilters: FilterData[],
  getDirent: (id: string) => Fs.Entry | undefined
): Fs.Dirent[] {
  const visibleTypes = visibleFilters.map(filter => filter.type);
  const isNoFiltersSelected = visibleFilters.length === 0;
  const isSearchTermEmpty = !searchTerm.trim() || searchTerm.trim().length < 3;

  if (isSearchTermEmpty && isNoFiltersSelected) {
    return dirents;
  }

  const filtered: Fs.Dirent[] = [];

  for (const dirent of dirents) {
    const direntEntry = getDirent(dirent.id);
    const nameMatches = dirent.name.toLowerCase().includes(searchTerm.toLowerCase());
    const descriptionMatches = direntEntry?.description?.toLowerCase().includes(searchTerm.toLowerCase());
    const typeMatches = isNoFiltersSelected || visibleTypes.includes(dirent.type);
    const childMatches = dirent.children ? filterTreeDirents(dirent.children, searchTerm, visibleFilters, getDirent) : [];

    const showBySearch = isSearchTermEmpty || nameMatches || descriptionMatches;

    if ((showBySearch && typeMatches) || childMatches.length > 0) {
      filtered.push({
        ...dirent,
        children: childMatches.length > 0 ? childMatches : dirent.children
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
  { label: 'Phone Numbers', type: 'phone' },
  { label: 'Languages', type: 'language' },
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
  const { isDarkMode } = useFsNav();

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