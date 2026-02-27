import { FilterData } from './useOwnerState';

export interface FsSearchProps {
  searchTerm: string;
  visibleFilters: FilterData[];
  open: boolean;
  onSearchChange: (value: string) => void;
  onFiltersChange: (filters: FilterData[]) => void;
}