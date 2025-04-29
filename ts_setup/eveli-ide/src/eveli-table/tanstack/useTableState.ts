import React from 'react';

import {
  ColumnDef, ColumnFiltersState, flexRender,
  getCoreRowModel, getFilteredRowModel, getPaginationRowModel, getSortedRowModel,
  SortingState, useReactTable, getFacetedUniqueValues,
  ColumnSizingState,
  VisibilityState,
  OnChangeFn,
  Updater,
} from '@tanstack/react-table';


import { tableSizeFn } from './tableSizeFn';




export function useTableState<DataType extends object>(
  props: {
    columns: ColumnDef<DataType, unknown>[],
    data: DataType[],
    options?: { initialPageSize?: number }
  }) {

    const initialPageSize = props.options?.initialPageSize ?? 20;

    const [sorting, setSorting] = React.useState<SortingState>([]); //{ id: 'priority', desc: true }
    const [pagination, setPagination] = React.useState({ pageIndex: 0, pageSize: initialPageSize });
    const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({});
    const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
    const [columnSizing, setColumnSizing] = React.useState<ColumnSizingState>({});
    const [filterDialogOpen, setFilterDialogOpen] = React.useState(false);
    

    const onColumnFilter = React.useCallback(() => {
      setFilterDialogOpen(prev => !prev);
    }, []);
  

    const onColumnVisibilityChange: OnChangeFn<VisibilityState> = React.useCallback((updaterOrValue: Updater<VisibilityState>) => {
      return setColumnVisibility(prev => {
  
  
        // @ts-ignore
        const newVisibility: Record<string, boolean> = updaterOrValue(prev);
        table.setColumnSizing(tableSizeFn(table, prev, newVisibility))
        return newVisibility;
      });
    }, []);
  
    const table = useReactTable({
      columns: props.columns,
      data: props.data,
      rowCount: props.data.length,
      columnResizeMode: 'onChange',
      enableColumnResizing: true,
      columnResizeDirection: 'ltr',
  
      getCoreRowModel: getCoreRowModel(),
      getSortedRowModel: getSortedRowModel(),
      getPaginationRowModel: getPaginationRowModel(),
      onPaginationChange: setPagination,
      onColumnVisibilityChange: onColumnVisibilityChange,
      onColumnFiltersChange: setColumnFilters,
      onColumnSizingChange: setColumnSizing,
      onSortingChange: setSorting,
      getFilteredRowModel: getFilteredRowModel(),
      getFacetedUniqueValues: getFacetedUniqueValues(),
  

      defaultColumn: {
        size: 150,
        minSize: 60,
        maxSize: 400
      },
      state: {
        columnVisibility,
        columnFilters,
        columnSizing,
        sorting,
        pagination
      },
  
    });
    
    const columnSizeVars = React.useMemo(() => {
      const headers = table.getFlatHeaders()
      const colSizes: { [key: string]: number } = {}
      for (let i = 0; i < headers.length; i++) {
        const header = headers[i]!
        colSizes[`--header-${header.id}-size`] = header.getSize()
        colSizes[`--col-${header.column.id}-size`] = header.column.getSize()
      }
      return colSizes
    }, [table.getState().columnSizingInfo, table.getState().columnSizing])
    
  return {
    columnSizeVars,
    table,
    pagination,
    initialPageSize,
    onColumnFilter,



    filterDialogOpen
  }
}