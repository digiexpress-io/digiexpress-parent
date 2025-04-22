import React from 'react';
import { Box } from '@mui/material';

import { ColumnDef, flexRender } from '@tanstack/react-table';

import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '@/api-task';
import { taskSortingFn } from './tableHelpers';
import { DateTime } from 'luxon';

import { IndicatorPriority } from './IndicatorPriority';
import { IndicatorStatus } from './IndicatorStatus';
import { IndicatorAssignee } from './IndicatorAssignee';
import { WithTableStyles } from './WithTableStyles';


export const TableTester: React.FC = () => {
  const { findAll } = useFetch('worker/rest/api/tasks.GET', {})
  const [data, setData] = React.useState<TaskApi.Task[]>([]);

  React.useEffect(() => {
    findAll().then(setData);
  }, []);


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
      enableHiding: false
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

  return (
    <Box display='flex'>
      <WithTableStyles data={data} columns={columns} />
    </Box>
  )

}
