import { FsNodeType } from '../fs-theme';
import { FilterData } from './useOwnerState';

export interface FsSearchProps {
  searchTerm: string;
  visibleFilters: FilterData[];
  open: boolean;
  onSearchChange: (value: string) => void;
  onFiltersChange: (filters: FilterData[]) => void;
}

export interface FsFilterChipProps {
  label: string;
  chipType: FsNodeType;
  isDarkMode: boolean;
}

export interface HighlightProps {
  text: string;
  searchTerm: string;
  isDarkMode: boolean
}