import React from 'react';
import { Box } from '@mui/material';
import { ColumnDef, flexRender } from '@tanstack/react-table';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';

import { TaskApi } from '@dxs-ts/task-api';
import { WithTableStyles } from '@dxs-ts/xui-table';
import { useTaskDashboard } from '../task-dashboard';


export const TaskAuditQueueBindingsTable: React.FC = () => {
  const intl = useIntl();
  const { task, taskAudit } = useTaskDashboard();
  const [bindings, setBindings] = React.useState<TaskApi.TaskAuditQueueBinding[]>([]);

  React.useEffect(() => {
    if (taskAudit.mq?.bindings) {
      setBindings(Object.values(taskAudit.mq.bindings));
      } else {
        console.log("oops, no messages!")
      }

  }, [taskAudit, task.id]);


  const columns: ColumnDef<TaskApi.TaskAuditQueueBinding, any>[] = [
    {
      header: intl.formatMessage({ id: 'task.audit.queueBindings.createdBy', defaultMessage: 'Created by' }),
      accessorKey: 'createdBy',
      size: 300,
      minSize: 200,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({ id: 'task.audit.queueBindings.status', defaultMessage: 'Status' }),
      accessorKey: 'status',
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({ id: 'task.audit.queueBindings.comment', defaultMessage: 'Comment' }),
      accessorKey: 'comment',
      size: 300,
      minSize: 200,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({ id: 'task.audit.queueBindings.createdAt', defaultMessage: 'Created' }),
      accessorKey: 'createdAt',
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (updated) => flexRender(AnyTaskDateTimeShort, { value: updated.getValue() })
    }
  ]

  return (
    <Box sx={{ display: 'inline-block' }}>
      <WithTableStyles data={bindings} columns={columns} options={{ tableId: 'taskAuditQueueBindings' }} />
    </Box>
  );
}


const AnyTaskDateTimeShort: React.FC<{ value: any }> = ({ value }) => {
  const rawDate = value;
  if (!rawDate) {
    return <div>--</div>
  }
  const dateTime = DateTime.fromISO(rawDate).setLocale('fi');
  const formatted = dateTime.toLocaleString(DateTime.DATETIME_SHORT_WITH_SECONDS);

  return <div>{formatted}</div>;
}