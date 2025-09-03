import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { WithTableStyles } from '@dxs-ts/xui-table';
import { Typography } from '@mui/material';
import { Box } from '@mui/system';
import { ColumnDef, flexRender } from '@tanstack/react-table';
import { DateTime } from 'luxon';
import React from 'react';
import { FormattedMessage, useIntl } from 'react-intl';
import { useTaskDashboard } from '../task-dashboard';




export const TaskAuditViewersTable: React.FC = () => {
  const intl = useIntl();
  const backend = useTaskBackend();
  const { task } = useTaskDashboard();
  const [viewers, setViewers] = React.useState<TaskApi.TaskViewer[]>([]);

  React.useEffect(() => {
    backend.persistence.getOneTaskAudit(task.id).then((audit) => {
      if (audit.access.value) {
        setViewers(audit.access.value);
        console.log("XXX")
      } else {
        console.log("oops, no viewers!")
      }
    });
  }, [backend, task.id]);



  const columns: ColumnDef<TaskApi.TaskViewer, any>[] = [
    {
      header: intl.formatMessage({id: 'task.audit.viewers.usedBy', defaultMessage: 'Viewed by'}),
      accessorKey: 'usedBy',
      //filterFn: filterFormattedDateFn,
      size: 300,
      minSize: 300,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({id: 'task.audit.viewers.updatedAt', defaultMessage: 'Updated at'}),
      accessorKey: 'updatedAt',
      //filterFn: filterFormattedDateFn,
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