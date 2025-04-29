import React from 'react';

import { ColumnDef, flexRender } from '@tanstack/react-table';

import { useTableState } from './tanstack';
import { EveliTable } from './table';

import { ToolColumnVisibilityDialog, ToolColumnVisibilitySelection } from './tool-column-visibility';
import { ToolPagination } from './tool-pagination';
import { ToolHeaderOptions } from './tool-header-options';
import { ToolColumnFilter } from './tool-column-filter';



export function WithTableStyles<DataType extends object>(props: {
  columns: ColumnDef<DataType, unknown>[],
  data: DataType[],
  options?: { initialPageSize?: number }
}): React.ReactNode {


  const {
    table, pagination, initialPageSize, columnSizeVars,
    onColumnFilter,

    filterDialogOpen

  } = useTableState<DataType>(props);

  return (
    <>
      <EveliTable
        slotProps={{
          root: { columnSizeVars },

          header: {
            cells: table.getFlatHeaders().map(header => ({
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
            children: <ToolPagination initialPageSize={initialPageSize} pagination={pagination} table={table} />
          },
          drawer: {
            body: (type) => {
              if (type === 'filters') {
                return <ToolColumnFilter status={<></>} priority={<></>} />
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
