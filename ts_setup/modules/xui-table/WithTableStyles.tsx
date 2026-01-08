import React from 'react';

import { flexRender, ColumnDef, RowData, Table } from '@tanstack/react-table';

import { useTable } from './tanstack';
import { EveliTable } from './table';

import { ToolColumnConfigDialog, ToolColumnConfig } from './tool-column-config';
import { ToolPagination } from './tool-pagination';
import { ToolHeaderOptions } from './tool-header-options';
import { ToolColumnFilter } from './tool-column-filter';
import { CircularProgress } from '@mui/material';
import { useTableState } from './table-state';
import { ToolColumnSavedFilter } from './tool-column-saved-filter';
import { TableState } from './table-api';
import { TableThemeProvider, TableThemeProviderProps } from './table-theme-provider';



// Register extra config params
declare module "@tanstack/react-table" {
  // @ts-ignore
  export interface ColumnMeta<TData extends RowData, TValue> {
    enableSelection?: boolean;
    enableDate?: boolean; // allow to cut data that is greate or equal then given date value

    slots?: {
      // filter type + render the type
      dateFilters?: Record<string, React.FC<{ onClick: (date:  Date | null) => void }>>
    }
  }
}

export interface TableSlots<DataType extends object> {
  drawer?: {
    'export-data'?: React.ElementType<{ table: Table<DataType> }>;
  }
}

export function WithTableStyles<DataType extends object>(props: {
  columns: ColumnDef<DataType, unknown>[];
  data: DataType[];
  options: { initialPageSize?: number, tableId: string };
  slots?: TableSlots<DataType>;
  theme?: Omit<TableThemeProviderProps, 'children'>
}): React.ReactNode {

  const { tableId } = props.options;
  const initialPageSize = props.options.initialPageSize ?? 20 
  const { loading, state } = useTableState({ initialPageSize, tableId, columns: props.columns });

  if(loading) {
    return <CircularProgress />
  }
  return (<RenderTable 
    theme={props.theme}
    columns={props.columns} 
    data={props.data} 
    options={{ initialPageSize, tableId }} 
    state={state} 
    slots={props.slots}/>)
}


function RenderTable<DataType extends object>(props: {
  columns: ColumnDef<DataType, unknown>[],
  data: DataType[],
  options: { initialPageSize: number, tableId: string  },
  state: [TableState, React.Dispatch<React.SetStateAction<TableState>>];
  slots: TableSlots<DataType> | undefined;
  theme?: Omit<TableThemeProviderProps, 'children'>
}): React.ReactNode {

  const { table, pagination, columnSizeVars, filterDialogOpen, onColumnFilter, onClearAll } = useTable<DataType>(props);
  const ExportDataSlot = props.slots?.drawer?.['export-data'];
  

  return (
    <TableThemeProvider {...(props.theme ? props.theme : {})}>
      <EveliTable slotProps={{
        root: { columnSizeVars },
        header: {
          cells: table.getFlatHeaders().filter(h => h.column.getIsVisible()).map(header => ({
            width: header.column.getSize(),
            title: flexRender(header.column.columnDef.header, header.getContext()),
            subTitle: <ToolHeaderOptions key={header.id} header={header} table={table} onColumnFilter={onColumnFilter} />
          }))
        },

        body: {
          rows: table.getRowModel().rows.map(row => ({
            cells: row.getVisibleCells().map(cell => ({
              width: cell.column.getSize(),
              children: flexRender(cell.column.columnDef.cell, cell.getContext())
            }))
          }))
        },

        footer: {
          pageSize: pagination.pageSize,
          children: <ToolPagination initialPageSize={props.options.initialPageSize} pagination={pagination} table={table} />
        },

        drawer: {
          body: {
            'export-data': ExportDataSlot ? <ExportDataSlot table={table} /> : undefined,
            'saved-filters': <ToolColumnSavedFilter table={table} state={props.state} tableId={props.options.tableId} onClearAll={onClearAll} />,
            'columns': <ToolColumnConfig table={table} />,
            'filters': <ToolColumnFilter table={table} />,
          }
        }
      }} />


      <>
        {/** Choose columns that are visible in full screen dialob */}
        <ToolColumnConfigDialog open={filterDialogOpen} onClose={onColumnFilter} table={table} />
      </>
    </TableThemeProvider>
  )
}