import React from 'react';
import { Box } from '@mui/material';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';
import { ColumnDef, flexRender } from '@tanstack/react-table';

import { TaskApi } from '@dxs-ts/task-api';
import { WithTableStyles } from '@dxs-ts/xui-table';
import { useTaskDashboard } from '../task-dashboard';



export const TaskAuditViewersTable: React.FC = () => {
  const intl = useIntl();
  const { task, taskAudit } = useTaskDashboard();
  const [viewers, setViewers] = React.useState<TaskApi.TaskViewer[]>([]);

  React.useEffect(() => {
    if (taskAudit.access.value) {
      setViewers(taskAudit.access.value);
      } else {
        console.log("oops, no viewers!")
      }

  }, [taskAudit, task.id]);



  const columns: ColumnDef<TaskApi.TaskViewer, any>[] = [
    {
      header: intl.formatMessage({id: 'task.audit.viewers.usedBy', defaultMessage: 'Viewed by'}),
      accessorKey: 'usedBy',
      size: 300,
      minSize: 300,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({id: 'task.audit.viewers.updatedAt', defaultMessage: 'Updated at'}),
      accessorKey: 'updatedAt',
      size: 300,
      minSize: 300,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (updated) => flexRender(AnyTaskDateTimeShort, { value: updated.getValue() })
    }
  ]

  return (
    <Box sx={{ display: 'inline-block' }}>
      <WithTableStyles data={viewers} columns={columns} options={{ tableId: 'taskAuditViewers' }} />
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