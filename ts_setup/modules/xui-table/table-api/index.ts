import {
  ColumnFiltersState,
  SortingState, 
  ColumnSizingState,
  VisibilityState,
  Updater,
  PaginationState,
  ColumnOrderState
} from '@tanstack/react-table';

export type EveliTableDrawerType = 'filters' | 'columns' | 'saved-filters' | 'export-data';

export interface EveliTableProps {
  slotProps: {
    root: {
      columnSizeVars: Record<string, number>
    },
    header: {
      cells: { width: number, title: React.ReactNode, subTitle: React.ReactNode }[];
    }
    body: {
      rows: { cells: { width: number, children: React.ReactNode }[] }[];
    },
    footer: {
      pageSize: number;
      children: React.ReactNode;
    }
    drawer: {
      body: Record<EveliTableDrawerType, React.ReactNode | undefined>;
    }
  }
}


export interface TableStateInitWith {
  sorting: SortingState;
  pagination: PaginationState;
  columnVisibility: VisibilityState;
  columnFilters: ColumnFiltersState;
  columnSizing: ColumnSizingState;
  columnOrder: ColumnOrderState;
  filterDialogOpen: boolean;
}

export interface TableState {
  sorting: SortingState;
  pagination: PaginationState;
  columnVisibility: VisibilityState;
  columnFilters: ColumnFiltersState;
  columnSizing: ColumnSizingState;
  columnOrder: ColumnOrderState;
  filterDialogOpen: boolean;

  hash: string;
  
  setSorting(next: Updater<SortingState>): TableState;
  setPagination(next: Updater<PaginationState>): TableState;
  setColumnVisibility(next: Updater<VisibilityState>): TableState;
  setColumnFilters(next: Updater<ColumnFiltersState>): TableState;
  setColumnSizing(next: Updater<ColumnSizingState>): TableState;
  setFilterDialogOpen(next: boolean): TableState;
  setColumnOrder(next: Updater<ColumnOrderState>): TableState;

  clear(): TableState;
  clearFiltersAndVisibility(): TableState;
  restore(props: TableStateInitWith): TableState;
  copy(): TableStateInitWith

  isActive: (init: TableStateInitWith) => boolean;
}


export interface TableDateFilter {
  date: Date | null, 
  type: 'EQUAL' | 'GTE' | 'LT'
}


export const anyDateFilter = (raw: Date | string | null | undefined, filter: TableDateFilter | string | Date) => {
  if (!raw) {
    return false;
  }
  
  // Parse the raw date
  const date = new Date(raw);
  if (isNaN(date.getTime())) {
    return false; // Invalid date
  }
  
  // Normalize filter to TableDateFilter format
  let normalizedFilter: TableDateFilter;
  
  if (typeof filter === 'string' || filter instanceof Date) {
    // If filter is string or Date, default to GTE
    normalizedFilter = {
      date: filter as any,
      type: 'GTE'
    };
  } else {
    // It's already a TableDateFilter
    normalizedFilter = filter;
  }
  
  if (!normalizedFilter.date) {
    return true;
  }
  
  // Parse the filter date
  const filterDate = new Date(normalizedFilter.date);
  if (isNaN(filterDate.getTime())) {
    return true; // Invalid filter date
  }
  
  // Default to 'GTE' if type is not specified
  const filterType = normalizedFilter.type || 'GTE';
  
  // Normalize both dates to midnight (start of day) for comparison
  const normalizedDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
  const normalizedFilterDate = new Date(filterDate.getFullYear(), filterDate.getMonth(), filterDate.getDate());
  
  if (filterType === 'GTE') {
    return normalizedDate >= normalizedFilterDate;
  }
  
  if (filterType === 'LT') {
    return normalizedDate < normalizedFilterDate;
  }
  
  if (filterType === 'EQUAL') {
    return normalizedDate.getTime() === normalizedFilterDate.getTime();
  }
  
  return false;
};