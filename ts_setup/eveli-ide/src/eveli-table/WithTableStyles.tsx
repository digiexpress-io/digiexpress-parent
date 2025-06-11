import React from 'react';

import { flexRender, ColumnDef, RowData } from '@tanstack/react-table';

import { useTable } from './tanstack';
import { EveliTable } from './table';

import { ToolColumnVisibilityDialog, ToolColumnVisibilitySelection } from './tool-column-visibility';
import { ToolPagination } from './tool-pagination';
import { ToolHeaderOptions } from './tool-header-options';
import { ToolColumnFilter } from './tool-column-filter';
import { CircularProgress } from '@mui/material';
import { TableState, useTableState } from './table-state';
import { ToolColumnSavedFilter } from './tool-column-saved-filter';



// Register extra config params
declare module "@tanstack/react-table" {
  export interface ColumnMeta<TData extends RowData, TValue> {
    enableSelection: boolean;
  }
}


export function WithTableStyles<DataType extends object>(props: {
  columns: ColumnDef<DataType, unknown>[],
  data: DataType[],
  options: { initialPageSize?: number, tableId: string }
}): React.ReactNode {

  const { tableId } = props.options;
  const initialPageSize = props.options.initialPageSize ?? 20 
  const { loading, state } = useTableState({ initialPageSize, tableId });

  if(loading) {
    return <CircularProgress />
  }
  return (<RenderTable columns={props.columns} data={props.data} options={{ initialPageSize, tableId }} state={state}/>)
}




function RenderTable<DataType extends object>(props: {
  columns: ColumnDef<DataType, unknown>[],
  data: DataType[],
  options: { initialPageSize: number, tableId: string  },
  state: [TableState, React.Dispatch<React.SetStateAction<TableState>>];
}): React.ReactNode {

  const { table, pagination, columnSizeVars, filterDialogOpen, onColumnFilter, onClearAll } = useTable<DataType>(props);

  return (
    <>
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
          body: (type) => {
            if (type === 'filters') {
              return <ToolColumnFilter table={table} onClearAll={onClearAll} />
            } else if(type === 'saved-filters') {
              return <ToolColumnSavedFilter table={table} state={props.state} tableId={props.options.tableId}/>
            }
            return (<ToolColumnVisibilitySelection table={table} />)
          }
        }
      }} />


      <>
        {/** Choose columns that are visible in full screen dialob */}
        <ToolColumnVisibilityDialog open={filterDialogOpen} onClose={onColumnFilter} table={table} />
      </>
    </>
  )

}