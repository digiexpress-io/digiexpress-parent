import React from 'react';

import { ColumnDef, flexRender } from '@tanstack/react-table';
import { DateTime } from 'luxon';
import { Box, Typography, IconButton, Tooltip } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useIntl, FormattedMessage } from 'react-intl';

import { useNavigate } from '@tanstack/react-router';

import { useFetch } from '@dxs-ts/envir-fetch';
import { TaskApi } from '@dxs-ts/task-api';
import { WithTableStyles, EveliPermissions } from '@dxs-ts/eveli-primitives';

import { IndicatorPriority } from './IndicatorPriority';
import { IndicatorStatus } from './IndicatorStatus';
import { IndicatorAssignee } from './IndicatorAssignee';
import { filterFormattedDateFn, filterStringOrArrayFn, filterTaskRefOrSubjectFn, taskSortingFn } from './tableHelpers';

import { TaskLink } from '../eveli-tasks/TaskLink';


export const EveliTasksTable: React.FC = () => {
  const intl = useIntl();
  const { findAll } = useFetch('worker/rest/api/tasks.GET', {})
  const [data, setData] = React.useState<TaskApi.Task[]>([]);
  const navigate = useNavigate();
  
  React.useEffect(() => {
    findAll().then(setData);
  }, []);

  const columns: ColumnDef<TaskApi.Task, any>[] = [
    {
      header: 'Priority',
      accessorKey: 'priority',
      filterFn: filterStringOrArrayFn,
      sortingFn: taskSortingFn,
      cell: (priority) => flexRender(IndicatorPriority, { type: priority.getValue() }),
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: {
        enableSelection: true
      }
    },
    {
      header: 'Name',
      accessorKey: 'subject',
      sortingFn: taskSortingFn,
      cell: (task) => flexRender(TaskLink, { title: task.getValue(), id: task.row.original.id, keywords: task.row.original.keyWords }),
      filterFn: filterTaskRefOrSubjectFn,
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableHiding: false,
      enableResizing: true,
      meta: {
        enableSelection: true
      }
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
      filterFn: 'includesString',
      size: 150,
      minSize: 150,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: 'Status',
      accessorKey: 'status',
      filterFn: filterStringOrArrayFn,
      size: 150,
      minSize: 150,
      cell: (status) => flexRender(IndicatorStatus, { status: status.getValue() }),
      sortingFn: taskSortingFn,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: {
        enableSelection: true
      }
    },
    {
      header: 'Roles',
      accessorKey: 'assignedRoles',
      filterFn: filterStringOrArrayFn,
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: {
        enableSelection: true
      }
    },
    {
      header: 'Assignee',
      accessorKey: 'assignedUser',
      filterFn: filterStringOrArrayFn,
      cell: (assignee) => flexRender(IndicatorAssignee, { name: assignee.getValue() }),
      sortingFn: taskSortingFn,
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: {
        enableSelection: true
      }
    },
    {
      header: 'Due',
      accessorKey: 'dueDate',
      filterFn: filterFormattedDateFn,
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (dueDate) => flexRender(AnyTaskDateTimeShort, { value: dueDate.getValue() })
    },
    {
      header: 'Created',
      accessorKey: 'created',
      filterFn: filterFormattedDateFn,
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (created) => flexRender(AnyTaskDateTimeShort, { value: created.getValue() })
    },
  ]

  return (
    <Box sx={{ display: 'inline-block' }}>
      <Box display="flex" alignItems="center" mb={2}>
        <Typography variant="h1" sx={{ flexGrow: 1 }}>
          <FormattedMessage id="tasksView.title" />
        </Typography>
        <EveliPermissions id="CREATE_TASK">
          <Tooltip title={intl.formatMessage({ id: 'taskButton.addTask' })}>
            <IconButton
              onClick={() => {
                navigate({
                  from: '/secured/$locale',
                  to: '/secured/$locale/worker/tasks/create',
                });
              }}
            >
              <AddIcon />
            </IconButton>
          </Tooltip>
        </EveliPermissions>
      </Box>
  
      <WithTableStyles data={data} columns={columns} options={{ tableId: 'tasks'}}/>
    </Box>
  );  
}

const AnyTaskDateTimeShort: React.FC<{ value: any }> = ({ value }) => {
  const rawDate = value;
  if (!rawDate) {
    return <div>--</div>
  }
  const dateTime = DateTime.fromISO(rawDate).setLocale('fi');
  const formatted = dateTime.toLocaleString(DateTime.DATE_SHORT);

  return <div>{formatted}</div>;
}
