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

import { EveliTable } from './EveliTable';
import { EveliTableHeader } from './EveliTableHeader';
import { EveliTableRow } from './EveliTableRow';
import { EveliTableHeaderCell } from './EveliTableHeaderCell';

import { EveliTablePagination } from './EveliTablePagination';
import { EveliTableColumnVisibilityDialog } from './EveliTableColumnVisibilityDialog';
import { EveliTableDrawer } from './EveliTableDrawer';
import { ColSelectItem, EveliTableColumnSelect } from './EveliTableColumnSelect';
import { EveliTableDrawerButtonColumn } from './EveliTableDrawerButtonColumn';
import { Box } from '@mui/system';
import { tableSizeFn } from './tableSizeFn';




export function WithTableStyles<DataType extends object>(props: {
  columns: ColumnDef<DataType, unknown>[],
  data: DataType[],
  options?: { initialPageSize?: number }
}): React.ReactNode {

  const initialPageSize = props.options?.initialPageSize ?? 5;
  const [sorting, setSorting] = React.useState<SortingState>([{ id: 'priority', desc: true }]);
  const [pagination, setPagination] = React.useState({ pageIndex: 0, pageSize: initialPageSize });
  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({});
  const [filterDialogOpen, setFilterDialogOpen] = React.useState(false);
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
  const [colsMenuOpen, setColsMenuOpen] = React.useState(false);
  const [filtersMenuOpen, setFiltersMenuOpen] = React.useState(false);
  const [columnSizing, setColumnSizing] = React.useState<ColumnSizingState>({});


  function toggleColsMenu() {
    setColsMenuOpen(prev => !prev);
    setFiltersMenuOpen(false);
  }
  function toggleFiltersMenu() {
    setFiltersMenuOpen(prev => !prev);
    setColsMenuOpen(false);
  }

  function toggleFilterDialogOpen() {
    setFilterDialogOpen(prev => !prev);
  }

  function clearColVisibility() {
    table.resetColumnVisibility();
  };

  const onColumnVisibilityChange: OnChangeFn<VisibilityState> = (updaterOrValue: Updater<VisibilityState>) => {
    return setColumnVisibility(prev => {


      // @ts-ignore
      const newVisibility: Record<string, boolean> = updaterOrValue(prev);
      table.setColumnSizing(tableSizeFn(table, prev, newVisibility))
      return newVisibility;
    });
  }


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

  const allColumns = table.getAllColumns().filter(col => col.getCanHide());

  return (
    <Box style={columnSizeVars} display='flex'>
      <EveliTableColumnVisibilityDialog open={filterDialogOpen} onClose={toggleFilterDialogOpen} table={table} />

      <EveliTable>
        {colsMenuOpen && <EveliTableDrawer title='Show / hide columns' onClose={toggleColsMenu}
          children={
            <EveliTableColumnSelect>
              {allColumns.map((col, index) => (<ColSelectItem colTitle={col.columnDef.header?.toString() || col.id} key={index}
                isVisible={col.getIsVisible()}
                onToggle={() => col.toggleVisibility()} />
              ))}
            </EveliTableColumnSelect>
          }
        />
        }
        {filtersMenuOpen && <EveliTableDrawer title='TODO' children={<>TODO</>} onClose={toggleFiltersMenu} />}

        <EveliTableHeader>
          {table.getFlatHeaders().map(header => (
            <EveliTableHeaderCell key={header.id}
              header={header}
              onColumnFilter={toggleFilterDialogOpen}
              onResetColVisibility={clearColVisibility}
            >
              {flexRender(header.column.columnDef.header, header.getContext())}
            </EveliTableHeaderCell>
          ))}
        </EveliTableHeader>

        {table.getRowModel().rows.map(row => (<EveliTableRow key={row.id} children={row} />))}

        <EveliTablePagination data={props.data} initialPageSize={initialPageSize} pagination={pagination} table={table} />
      </EveliTable>

      <EveliTableDrawerButtonColumn onColumnsClick={toggleColsMenu} onFiltersClick={toggleFiltersMenu} />
    </Box>
  )

}
