import React from 'react';
import { Box } from '@mui/material';

import {
  ColumnDef, createColumnHelper, flexRender,
  getCoreRowModel, getPaginationRowModel, getSortedRowModel,
  SortingState, useReactTable
} from '@tanstack/react-table';

import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '@/api-task';

import { EveliTable } from './EveliTable';
import { EveliTableCell } from './EveliTableCell';
import { EveliTableHeaderCell } from './EveliTableHeaderCell';

import { IndicatorPriority } from './IndicatorPriority';
import { IndicatorStatus } from './IndicatorStatus';
import { IndicatorAssignee } from './IndicatorAssignee';

import { EveliTableHeaderRoot, EveliTableRowRoot } from './useUtilityClasses';
import { EveliTableDrawerButtonColumn } from './EveliTableDrawerButtonColumn';

import { taskSortingFn } from './tableSorters';
import { DateTime } from 'luxon';
import { EveliTablePagination } from './EveliTablePagination';
import { EveliTableColumnFilterDialog } from './EveliTableColumnFilterDialog';


const initialPageSize = 5;

export const TableTester: React.FC = () => {
  const { findAll } = useFetch('worker/rest/api/tasks.GET', {})
  const [data, setData] = React.useState<TaskApi.Task[]>([]);
  const [sorting, setSorting] = React.useState<SortingState>([{ id: 'priority', desc: true }]);
  const [pagination, setPagination] = React.useState({ pageIndex: 0, pageSize: initialPageSize });
  const [columnVisibility, setColumnVisibility] = React.useState({});
  const [filterDialogOpen, setFilterDialogOpen] = React.useState(false);

  React.useEffect(() => {
    findAll().then(setData);
  }, []);


  function toggleFilterDialogOpen() {
    setFilterDialogOpen(prev => !prev);
  }

  //const columnHelper = createColumnHelper<TaskApi.Task>();

  const columns: ColumnDef<TaskApi.Task, any>[] = [

    {
      header: 'Priority',
      accessorKey: 'priority',
      enableSorting: true,
      sortingFn: taskSortingFn,
      cell: (priority) => flexRender(IndicatorPriority, { type: priority.getValue() }),
      footer: 'footer 1'
    },
    {
      header: 'Name',
      accessorKey: 'subject',
      sortingFn: taskSortingFn
    },
    {
      header: 'Info',
      accessorKey: 'additionalInfo',
      enableSorting: false,
      size: 100,
      enableResizing: false
    },
    {
      header: 'Client',
      accessorKey: 'clientIdentificator',
      footer: 'footer 2'
    },
    {
      header: 'Status',
      accessorKey: 'status',
      enableSorting: true,
      cell: (status) => flexRender(IndicatorStatus, { type: status.getValue() }),
      sortingFn: taskSortingFn
    },
    {
      header: 'Roles',
      accessorKey: 'assignedRoles',
      enableSorting: false,
    },
    {
      header: 'Assignee',
      accessorKey: 'assignedUser',
      cell: (assignee) => flexRender(IndicatorAssignee, { name: assignee.getValue() }),
      sortingFn: taskSortingFn
    },
    {
      header: 'Due',
      accessorKey: 'dueDate',
      cell: (info) => {
        const rawDate = info.getValue();
        if (!rawDate) return (<div>–</div>)

        const dateTime = DateTime.fromISO(rawDate).setLocale('fi');
        const formatted = dateTime.toLocaleString(DateTime.DATE_SHORT);
        return <>{formatted}</>;
      }
    },
    {
      header: 'Created',
      accessorKey: 'created',
      cell: (info) => {
        const rawDate = info.getValue();
        const dateTime = DateTime.fromISO(rawDate).setLocale('fi');
        const formatted = dateTime.toLocaleString(DateTime.DATE_SHORT);
        return <>{formatted}</>;
      }
    },

  ]

  const table = useReactTable({
    columns,
    data,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    onPaginationChange: setPagination,
    onColumnVisibilityChange: setColumnVisibility,
    rowCount: data.length,
    onSortingChange: setSorting,

    state: {
      columnVisibility,
      sorting,
      pagination
    },
  })

  return (
    <Box display='flex'>
      <EveliTableColumnFilterDialog open={filterDialogOpen} onClose={toggleFilterDialogOpen} columns={columns} table={table} />

      <EveliTable>
        {table.getHeaderGroups().map(headerGroup => {
          //const width = headerGroup.headers.map(header => header.getSize()).reduce((partialSum, a) => partialSum + a, 0);

          return (
            <EveliTableHeaderRoot key={headerGroup.id}>
              {headerGroup.headers.map(header => {
                return (
                  <EveliTableHeaderCell key={header.id}
                    column={header.column}
                    sortDirection={header.column.getIsSorted()}
                    onColumnFilter={toggleFilterDialogOpen}
                  >
                    {header.isPlaceholder ? null : flexRender(header.column.columnDef.header, header.getContext())}
                  </EveliTableHeaderCell>
                )
              })}
            </EveliTableHeaderRoot>
          )
        })}

        {table.getRowModel().rows.map(row => (
          <EveliTableRowRoot key={row.id}>
            {row.getVisibleCells().map(cell => {
              return (
                <EveliTableCell key={cell.id} width={cell.column.getSize()}>
                  {flexRender(cell.column.columnDef.cell, cell.getContext())}
                </EveliTableCell>
              )
            })}
          </EveliTableRowRoot>
        ))}

        <EveliTablePagination data={data} initialPageSize={initialPageSize} pagination={pagination} table={table} />
      </EveliTable>

      <EveliTableDrawerButtonColumn onColumnsClick={() => { }} onFiltersClick={() => { }} />
    </Box>
  )

}
