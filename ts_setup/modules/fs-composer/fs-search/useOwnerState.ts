import React from 'react';
import { useFsTheme } from '../fs-theme';
import type { AssetTypeFilter, FilterData } from './search-helpers';
import { FsSearchProps } from './FsSearchProps';
import { useFsSearch } from './FsSearchProvider';

export type { FilterData, AssetTypeFilter };

const SEARCH_DEBOUNCE_MS = 350;

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
  const { isDarkMode } = useFsTheme();
  const { search } = useFsSearch();
  const [inputValue, setInputValue] = React.useState(search.searchTerm);
  const debounceRef = React.useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const value = event.target.value;
    setInputValue(value);
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }
    debounceRef.current = setTimeout(() => {
      search.setSearchTerm(value);
    }, SEARCH_DEBOUNCE_MS);
  };

  const handleTypeFilterSelectChange = (selectedLabels: string[]) => {
    search.handleFilterSelectChange(selectedLabels);
  };

  const handleLabelFilterSelectChange = (selectedValues: string[]) => {
    search.handleLabelFilterSelectChange(selectedValues);
  };

  return {
    isDarkMode,

    searchTerm: inputValue,
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
