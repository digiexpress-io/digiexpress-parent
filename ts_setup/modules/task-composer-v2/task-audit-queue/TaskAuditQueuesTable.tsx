import React from 'react';
import { Box } from '@mui/material';
import { ColumnDef, flexRender } from '@tanstack/react-table';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';

import { TaskApi } from '@dxs-ts/task-api';
import { WithTableStyles } from '@dxs-ts/xui-table';
import { useTaskDashboard } from '../task-dashboard';


export const TaskAuditQueuesTable: React.FC = () => {
  const intl = useIntl();
  const { task, taskAudit } = useTaskDashboard();
  const [queues, setQueues] = React.useState<TaskApi.TaskAuditQueue[]>([]);

  React.useEffect(() => {
    if (taskAudit.mq) {
      setQueues(Object.values(taskAudit.mq.queues));
      } else {
        console.log("oops, no queues!")
      }

  }, [taskAudit, task.id]);

  console.log(queues)

  const columns: ColumnDef<TaskApi.TaskAuditQueue, any>[] = [
    {
      header: intl.formatMessage({ id: 'task.audit.queue.name', defaultMessage: 'Queue name' }),
      accessorKey: 'queueName',
      size: 200,
      minSize: 200,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({ id: 'task.audit.queue.createdBy', defaultMessage: 'Created by' }),
      accessorKey: 'createdBy',
      size: 250,
      minSize: 200,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({ id: 'task.audit.queue.comment', defaultMessage: 'Comment' }),
      accessorKey: 'comment',
      size: 250,
      minSize: 200,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({ id: 'task.audit.queue.createdAt', defaultMessage: 'Created' }),
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
      <WithTableStyles data={queues} columns={columns} options={{ tableId: 'taskAuditQueues', initialPageSize: 10 }} />
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