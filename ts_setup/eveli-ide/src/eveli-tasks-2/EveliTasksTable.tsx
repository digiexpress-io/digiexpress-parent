import React from 'react';

import { ColumnDef, flexRender } from '@tanstack/react-table';

import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '@/api-task';
import { taskSortingFn } from './tableHelpers';
import { DateTime } from 'luxon';

import { IndicatorPriority } from './IndicatorPriority';
import { IndicatorStatus } from './IndicatorStatus';
import { IndicatorAssignee } from './IndicatorAssignee';
import { WithTableStyles } from '@/eveli-table';



export const EveliTasksTable: React.FC = () => {

  const { findAll } = useFetch('worker/rest/api/tasks.GET', {})
  const [data, setData] = React.useState<TaskApi.Task[]>([]);

  React.useEffect(() => {
    findAll().then(setData);
  }, []);


  const columns: ColumnDef<TaskApi.Task, any>[] = [
    {
      header: 'Priority',
      accessorKey: 'priority',
      filterFn: 'arrIncludesSome',
      sortingFn: taskSortingFn,
      cell: (priority) => flexRender(IndicatorPriority, { type: priority.getValue() }),
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: 'Name',
      accessorKey: 'subject',
      sortingFn: taskSortingFn,
      filterFn: 'arrIncludes',
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableHiding: false,
      enableResizing: true,
    },
    {
      header: 'Info',
      accessorKey: 'additionalInfo',
      size: 100,
      minSize: 100,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
    },
    {
      header: 'Client',
      accessorKey: 'clientIdentificator',
      filterFn: 'arrIncludesSome',
      size: 150,
      minSize: 150,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: 'Status',
      accessorKey: 'status',
      filterFn: 'arrIncludesSome',
      size: 150,
      minSize: 150,
      cell: (status) => flexRender(IndicatorStatus, { type: status.getValue() }),
      sortingFn: taskSortingFn,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: 'Roles',
      accessorKey: 'assignedRoles',
      filterFn: 'arrIncludesSome',
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: 'Assignee',
      accessorKey: 'assignedUser',
      filterFn: 'arrIncludesSome',
      cell: (assignee) => flexRender(IndicatorAssignee, { name: assignee.getValue() }),
      sortingFn: taskSortingFn,
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: 'Due',
      accessorKey: 'dueDate',
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: false,
      enableResizing: true,

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
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: false,
      enableResizing: true,

      cell: (info) => {
        const rawDate = info.getValue();
        const dateTime = DateTime.fromISO(rawDate).setLocale('fi');
        const formatted = dateTime.toLocaleString(DateTime.DATE_SHORT);
        return <>{formatted}</>;
      }
    },
  ]


  return (<WithTableStyles data={data} columns={columns} />)
}
