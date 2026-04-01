import React from 'react';
import { useFsNav, useFsDirent } from '@dxs-ts/fs-api';
import type { FilterData } from './search-helpers';
import { FsSearchProps } from './FsSearchProps';
import { useFsSearch } from './FsSearchProvider';

export type { FilterData };



export interface OwnerState {
  searchTerm: string;
  visibleFilters: FilterData[];
  open: boolean;
  onSearchChange: (value: string) => void;
  onFiltersChange: (filters: FilterData[]) => void;
  availableLabelOptions: string[];
  isDarkMode: boolean;
  allAvailableFilters: FilterData[];
  handleSearchChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
  handleFilterSelectChange: (selectedLabels: string[]) => void;
}

export const useOwnerState = (_props: FsSearchProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const { selectOptions } = useFsDirent();
  const { search } = useFsSearch();

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    search.handleSearchChange(event);
  };

  const handleFilterSelectChange = (selectedLabels: string[]) => {
    search.handleFilterSelectChange(selectedLabels);
  };

  return {
    isDarkMode,

    searchTerm: search.searchTerm,
    visibleFilters: search.activeFilters,
    open: search.open,
    allAvailableFilters: search.allAvailableFilters,
    availableLabelOptions: selectOptions.labels,

    handleSearchChange,
    handleFilterSelectChange,

    onSearchChange: search.setSearchTerm,
    onFiltersChange: search.setActiveFilters,
  };
};
