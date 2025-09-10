
import {
  ColumnFiltersState,
  SortingState, 
  ColumnSizingState,
  VisibilityState,
  Updater,
  PaginationState,
  ColumnOrderState
} from '@tanstack/react-table';


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