import React from 'react';
import { Box } from '@mui/material';

import {
  ColumnDef, ColumnFiltersState, createColumnHelper, flexRender,
  getCoreRowModel, getFilteredRowModel, getPaginationRowModel, getSortedRowModel,
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

import { taskSortingFn } from './tableHelpers';
import { DateTime } from 'luxon';
import { EveliTablePagination } from './EveliTablePagination';
import { EveliTableColumnFilterDialog } from './EveliTableColumnFilterDialog';
import { EveliTableDrawer } from './EveliTableDrawer';
import { ColSelectItem, EveliTableColSelect } from './EveliTableColSelect';
import { EveliTableColumnFilter } from './EveliTableColumnFilter';


const initialPageSize = 5;

export const TableTester: React.FC = () => {
  const { findAll } = useFetch('worker/rest/api/tasks.GET', {})
  const [data, setData] = React.useState<TaskApi.Task[]>([]);
  const [sorting, setSorting] = React.useState<SortingState>([{ id: 'priority', desc: true }]);
  const [pagination, setPagination] = React.useState({ pageIndex: 0, pageSize: initialPageSize });
  const [columnVisibility, setColumnVisibility] = React.useState({});
  const [filterDialogOpen, setFilterDialogOpen] = React.useState(false);
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
  const [colsMenuOpen, setColsMenuOpen] = React.useState(false);
  const [filtersMenuOpen, setFiltersMenuOpen] = React.useState(false);


  function toggleColsMenu() {
    setColsMenuOpen(prev => !prev);
    setFiltersMenuOpen(false);
  }
  function toggleFiltersMenu() {
    setFiltersMenuOpen(prev => !prev);
    setColsMenuOpen(false);
  }
  React.useEffect(() => {
    findAll().then(setData);
  }, []);

  function toggleFilterDialogOpen() {
    setFilterDialogOpen(prev => !prev);
  }

  function clearColVisibility() {
    table.resetColumnVisibility();
  };


  //const columnHelper = createColumnHelper<TaskApi.Task>();

  const columns: ColumnDef<TaskApi.Task, any>[] = [
    {
      header: 'Priority',
      accessorKey: 'priority',
      enableSorting: true,
      enableColumnFilter: true,
      sortingFn: taskSortingFn,
      cell: (priority) => flexRender(IndicatorPriority, { type: priority.getValue() }),
      footer: 'footer 1',
    },
    {
      header: 'Name',
      accessorKey: 'subject',
      sortingFn: taskSortingFn,
      filterFn: 'includesString',
      enableSorting: true,
      enableColumnFilter: true,
    },
    {
      header: 'Info',
      accessorKey: 'additionalInfo',
      enableSorting: false,
      size: 150,
      enableResizing: false,
      enableColumnFilter: true,
    },
    {
      header: 'Client',
      accessorKey: 'clientIdentificator',
      footer: 'footer 2',
      enableColumnFilter: true,
    },
    {
      header: 'Status',
      accessorKey: 'status',
      cell: (status) => flexRender(IndicatorStatus, { type: status.getValue() }),
      sortingFn: taskSortingFn,
      enableSorting: true,
      enableColumnFilter: true,
    },
    {
      header: 'Roles',
      accessorKey: 'assignedRoles',
      enableSorting: true,
      enableColumnFilter: true,
    },
    {
      header: 'Assignee',
      accessorKey: 'assignedUser',
      cell: (assignee) => flexRender(IndicatorAssignee, { name: assignee.getValue() }),
      sortingFn: taskSortingFn,
      enableSorting: true,
      enableColumnFilter: true,
    },
    {
      header: 'Due',
      accessorKey: 'dueDate',
      enableSorting: true,
      enableColumnFilter: false,

      cell: (info) => {
        const rawDate = info.getValue();
        if (!rawDate) return (<div>–</div>)

        const dateTime = DateTime.fromISO(rawDate).setLocale('fi');
        const formatted = dateTime.toLocaleString(DateTime.DATE_SHORT);
        return <>{formatted}</>;
      },

    },
    {
      header: 'Created',
      accessorKey: 'created',
      enableSorting: true,
      enableColumnFilter: false,

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
    rowCount: data.length,

    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    onPaginationChange: setPagination,
    onColumnVisibilityChange: setColumnVisibility,
    onColumnFiltersChange: setColumnFilters,
    onSortingChange: setSorting,
    getFilteredRowModel: getFilteredRowModel(),

    state: {
      columnVisibility,
      columnFilters,
      sorting,
      pagination
    },
  })

  const allColumns = table.getAllColumns().filter(col => col.getCanHide());

  return (
    <Box display='flex'>
      <EveliTableColumnFilterDialog open={filterDialogOpen} onClose={toggleFilterDialogOpen} columns={columns} table={table} />

      <EveliTable>
        {colsMenuOpen && <EveliTableDrawer
          children={
            <EveliTableColSelect>
              Show / Hide columns
              {allColumns.map((col, index) => (<ColSelectItem colTitle={col.columnDef.header?.toString() || col.id} key={index}
                isVisible={col.getIsVisible()}
                onToggle={() => col.toggleVisibility()} />
              )
              )}
            </EveliTableColSelect>
          }
        />
        }
        {filtersMenuOpen && <EveliTableDrawer children={<>TODO</>} />}

        {table.getHeaderGroups().map(headerGroup => {
          //const width = headerGroup.headers.map(header => header.getSize()).reduce((partialSum, a) => partialSum + a, 0);

          return (
            <EveliTableHeaderRoot key={headerGroup.id}>
              {headerGroup.headers.map(header => {
                return (
                  <EveliTableHeaderCell
                    filterComponent={<EveliTableColumnFilter filterItems={['filter 1']} column={header.column} />}
                    key={header.id} column={header.column}
                    isFilterable={header.column.getCanFilter()}
                    isSortable={header.column.getCanSort()}
                    sortDirection={header.column.getIsSorted()}
                    onColumnFilter={toggleFilterDialogOpen}
                    onResetColVisibility={clearColVisibility}
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
                <EveliTableCell key={cell.id} width={cell.column.getSize()} column={cell.column}>
                  {flexRender(cell.column.columnDef.cell, cell.getContext())}
                </EveliTableCell>
              )
            })}
          </EveliTableRowRoot>
        ))}
        <EveliTablePagination data={data} initialPageSize={initialPageSize} pagination={pagination} table={table} />
      </EveliTable>

      <EveliTableDrawerButtonColumn onColumnsClick={toggleColsMenu} onFiltersClick={toggleFiltersMenu} />
    </Box>
  )

}
