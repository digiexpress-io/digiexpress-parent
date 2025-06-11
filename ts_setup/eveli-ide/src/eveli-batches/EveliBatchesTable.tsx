import React from 'react';

import { ColumnDef, flexRender } from '@tanstack/react-table';

import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '@/api-task';
import { filterFormattedDateFn, filterStringOrArrayFn, filterTaskRefOrSubjectFn, taskSortingFn } from './tableHelpers';
import { DateTime } from 'luxon';

import { IndicatorPriority } from './IndicatorPriority';
import { IndicatorStatus } from './IndicatorStatus';
import { IndicatorAssignee } from './IndicatorAssignee';
import { WithTableStyles } from '@/eveli-table';
import { TaskLink } from '@/eveli-tasks/TaskLink';

import { Box, Typography, IconButton, Tooltip } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useIntl, FormattedMessage } from 'react-intl';

import { useNavigate } from '@tanstack/react-router';
import { EveliPermissions } from '@/eveli-permissions';



export const EveliBatchesTable: React.FC = () => {
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
    }
  ]

  return (
    <Box sx={{ display: 'inline-block' }}>
      <Box display="flex" alignItems="center" mb={2}>
        <Typography variant="h1" sx={{ flexGrow: 1 }}>
          <FormattedMessage id="batchesView.title" />
        </Typography>

          <Tooltip title={intl.formatMessage({ id: 'taskButton.addInstance' })}>
            <IconButton
              onClick={() => {
                navigate({
                  from: '/secured/$locale',
                  to: '/secured/$locale/worker/batches/create',
                });
              }}
            >
              <AddIcon />
            </IconButton>
          </Tooltip>

      </Box>
  
      <WithTableStyles data={data} columns={columns} options={{ tableId: 'batches'}}/>
    </Box>
  );  
}

