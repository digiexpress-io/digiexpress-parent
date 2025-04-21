import React from 'react';
import { Box } from '@mui/material';

import { ColumnDef, createColumnHelper, flexRender, getCoreRowModel, getPaginationRowModel, getSortedRowModel, SortingState, useReactTable } from '@tanstack/react-table'
import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '@/api-task';
import { EveliTable, EveliTableCell, EveliTableHeaderCell1 } from './EveliTable';
import { IndicatorPriority } from './IndicatorPriority';
import { IndicatorStatus } from './IndicatorStatus';
import { IndicatorAssignee } from './IndicatorAssignee';

import { EveliTableHeaderRoot, EveliTableRowRoot } from './useUtilityClasses';
import { EveliTableDrawerButtonColumn } from './EveliTableDrawerButtonColumn';

import { taskSortingFn } from './tableSorters';
import { DateTime } from 'luxon';
import { EveliTablePagination } from './EveliTablePagination';


const initialPageSize = 4;

export const TableTester: React.FC = () => {
  const { findAll } = useFetch('worker/rest/api/tasks.GET', {})
  const [data, setData] = React.useState<TaskApi.Task[]>([]);
  const [sorting, setSorting] = React.useState<SortingState>([])
  const [pagination, setPagination] = React.useState({
    pageIndex: 0, //initial page index
    pageSize: initialPageSize, //default page size
  });

  React.useEffect(() => {
    findAll().then(setData);
  }, []);



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
      size: 200,
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
      enableSorting: false
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
    rowCount: data.length,
    onSortingChange: setSorting,
    state: {
      sorting,
      pagination
    },
  })

  return (
    <Box display='flex'>
      <EveliTable>
        {table.getHeaderGroups().map(headerGroup => {
          //const width = headerGroup.headers.map(header => header.getSize()).reduce((partialSum, a) => partialSum + a, 0);

          return (
            <EveliTableHeaderRoot key={headerGroup.id}>
              {headerGroup.headers.map(header => {
                return (
                  <EveliTableHeaderCell1 key={header.id} column={header.column} sortDirection={header.column.getIsSorted()}>
                    {header.isPlaceholder ? null : flexRender(header.column.columnDef.header, header.getContext())}
                  </EveliTableHeaderCell1>
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
