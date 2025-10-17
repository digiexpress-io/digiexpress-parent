import React from 'react';

import {
  ColumnDef, ColumnFiltersState,
  getCoreRowModel, getFilteredRowModel, getPaginationRowModel, getSortedRowModel,

  SortingState, useReactTable, getFacetedUniqueValues,
  ColumnSizingState,
  VisibilityState,
  OnChangeFn,
  Updater,
  PaginationState,
  ColumnOrderState
} from '@tanstack/react-table';

import { TableState } from '../table-api';


export function useTable<DataType extends object>(
  props: {
    columns: ColumnDef<DataType, unknown>[],
    data: DataType[],
    options: { initialPageSize: number },
    state: [TableState, React.Dispatch<React.SetStateAction<TableState>>];
  }) {

  const [state, setState] = props.state;

  const onColumnFilter = React.useCallback(() => setState(prev => prev.setFilterDialogOpen(!prev.filterDialogOpen)), []);
  const onPaginationChange: OnChangeFn<PaginationState> = React.useCallback((pagination) => setState(prev => prev.setPagination(pagination)), []);
  
  const onColumnFiltersChange: OnChangeFn<ColumnFiltersState> = React.useCallback((p) => setState(prev => prev.setColumnFilters(p)), []);
  const onColumnSizingChange: OnChangeFn<ColumnSizingState> = React.useCallback((p) => setState(prev => prev.setColumnSizing(p)), []);
  const onSortingChange: OnChangeFn<SortingState> = React.useCallback((p) => setState(prev => prev.setSorting(p)), []);
  const onColumnOrderChange: OnChangeFn<ColumnOrderState> = React.useCallback((p) => setState(prev => prev.setColumnOrder(p)), []);

  const onColumnVisibilityChange: OnChangeFn<VisibilityState> = React.useCallback((updaterOrValue: Updater<VisibilityState>) => 
    setState(state => {
      const prev = state.columnVisibility;
      const newVisibility = typeof updaterOrValue === 'function' ? updaterOrValue(prev) : updaterOrValue;
      // TODO:: state managment bug??  table.setColumnSizing(tableSizeFn(table, prev, newVisibility));
      return state.setColumnVisibility(newVisibility);
    }), []);

  const onClearAll = React.useCallback(() => setState(state => state.clear()), [])
  const resetTableColsAndFilters = React.useCallback(() => setState(state => state.clearFiltersAndVisibility()), [])



  const table = useReactTable({
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getFacetedUniqueValues: getFacetedUniqueValues(),

    onColumnVisibilityChange,
    onPaginationChange,
    onColumnFiltersChange,
    onColumnSizingChange,
    onSortingChange,
    onColumnOrderChange,

    columns: props.columns,
    data: props.data,
    rowCount: props.data.length,
    columnResizeMode: 'onChange',
    enableColumnResizing: true,
    columnResizeDirection: 'ltr',
    defaultColumn: {
      size: 150,
      minSize: 60,
      maxSize: 400
    },
    state: {
      columnVisibility: state.columnVisibility,
      columnFilters: state.columnFilters,
      columnSizing: state.columnSizing,
      sorting: state.sorting,
      pagination: state.pagination,
      columnOrder: state.columnOrder
    }
  });

  const columnSizeVars = React.useMemo(() => {
    const headers = table.getFlatHeaders().filter(header => header.column.getIsVisible())
    const colSizes: { [key: string]: number } = {}
    for (let i = 0; i < headers.length; i++) {
      const header = headers[i]!
      colSizes[`--header-${header.id}-size`] = header.getSize()
      colSizes[`--col-${header.column.id}-size`] = header.column.getSize()
    }
    return colSizes
  }, [table.getState().columnSizingInfo, table.getState().columnSizing])

  return {
    columnSizeVars, table,
    pagination: state.pagination,
    filterDialogOpen: state.filterDialogOpen,
    onColumnFilter,
    resetTableColsAndFilters,
    onClearAll,
  }
}