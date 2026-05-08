import React from 'react';
import { useFsNav } from '@dxs-ts/fs-nav';
import type { AssetTypeFilter, FilterData } from './search-helpers';
import { FsSearchProps } from './FsSearchProps';
import { useFsSearch } from './FsSearchProvider';

export type { FilterData, AssetTypeFilter };



export interface OwnerState {
  searchTerm: string;
  visibleFilters: FilterData[];
  open: boolean;
  onSearchChange: (value: string) => void;
  onFiltersChange: (filters: FilterData[]) => void;
  availableLabelOptions: string[];
  isDarkMode: boolean;
  allAvailableTypeFilters: AssetTypeFilter[];
  handleSearchChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
  handleTypeFilterSelectChange: (selectedLabels: string[]) => void;
  handleLabelFilterSelectChange: (selectedValues: string[]) => void;
}

export const useOwnerState = (_props: FsSearchProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const { search } = useFsSearch();

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    search.handleSearchChange(event);
  };

  const handleTypeFilterSelectChange = (selectedLabels: string[]) => {
    search.handleFilterSelectChange(selectedLabels);
  };

  const handleLabelFilterSelectChange = (selectedValues: string[]) => {
    search.handleLabelFilterSelectChange(selectedValues);
  };

  return {
    isDarkMode,

    searchTerm: search.searchTerm,
    visibleFilters: search.activeFilters,
    open: search.open,
    allAvailableTypeFilters: search.allAvailableTypeFilters,
    availableLabelOptions: search.availableLabelOptions,

    handleSearchChange,
    handleTypeFilterSelectChange,
    handleLabelFilterSelectChange,

    onSearchChange: search.setSearchTerm,
    onFiltersChange: search.setActiveFilters,
  };
};
