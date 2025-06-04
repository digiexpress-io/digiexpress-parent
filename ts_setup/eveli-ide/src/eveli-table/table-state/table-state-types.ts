
import {
  ColumnFiltersState,
  SortingState, 
  ColumnSizingState,
  VisibilityState,
  Updater,
  PaginationState
} from '@tanstack/react-table';




export interface TableState {
  sorting: SortingState;
  pagination: PaginationState;
  columnVisibility: VisibilityState;
  columnFilters: ColumnFiltersState;
  columnSizing: ColumnSizingState;
  filterDialogOpen: boolean;
  hash: string;
  
  setSorting(next: Updater<SortingState>): TableState;
  setPagination(next: Updater<PaginationState>): TableState;
  setColumnVisibility(next: Updater<VisibilityState>): TableState;
  setColumnFilters(next: Updater<ColumnFiltersState>): TableState;
  setColumnSizing(next: Updater<ColumnSizingState>): TableState;
  setFilterDialogOpen(next: boolean): TableState;

  clear(): TableState;
  clearFiltersAndVisibility(): TableState;
  restore(props: {
    sorting: SortingState;
    pagination: PaginationState;
    columnVisibility: VisibilityState;
    columnFilters: ColumnFiltersState;
    columnSizing: ColumnSizingState;
    filterDialogOpen: boolean;  
  }):TableState;

  copy(): {
    sorting: SortingState;
    pagination: PaginationState;
    columnVisibility: VisibilityState;
    columnFilters: ColumnFiltersState;
    columnSizing: ColumnSizingState;
    filterDialogOpen: boolean;  
  }
}