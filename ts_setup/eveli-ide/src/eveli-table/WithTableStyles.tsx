import React from 'react';

import { flexRender, ColumnDef, RowData } from '@tanstack/react-table';

import { useTableState } from './tanstack';
import { EveliTable } from './table';

import { ToolColumnVisibilityDialog, ToolColumnVisibilitySelection } from './tool-column-visibility';
import { ToolPagination } from './tool-pagination';
import { ToolHeaderOptions } from './tool-header-options';
import { ToolColumnFilter } from './tool-column-filter';



// Register extra config params
declare module "@tanstack/react-table" {
  export interface ColumnMeta<TData extends RowData, TValue> {
    enableSelection: boolean;
  }
}


export function WithTableStyles<DataType extends object>(props: {
  columns: ColumnDef<DataType, unknown>[],
  data: DataType[],
  options?: { initialPageSize?: number }
}): React.ReactNode {


  const {
    table, pagination, initialPageSize, columnSizeVars, filterDialogOpen,
    onColumnFilter, onClearAll,
  } = useTableState<DataType>(props);

  return (
    <>
      <EveliTable
        slotProps={{
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
            children: <ToolPagination initialPageSize={initialPageSize} pagination={pagination} table={table} />
          },
          drawer: {
            body: (type) => {
              if (type === 'filters') {
                return <ToolColumnFilter table={table} onClearAll={onClearAll} />
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
