import React from 'react';
import { Box } from '@mui/material';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';

import { WithTableStyles } from '@dxs-ts/xui-table';
import { ColumnDef, flexRender } from '@tanstack/react-table';
import { useTaskDashboard } from '../task-dashboard';


interface ProcessEntry {  
  type: string,
  value: string
  targetDate: string | undefined
}

const datetypes: string[] = ['created', 'updated', 'expiresAt'];


export const TaskAuditProcessesTable: React.FC = () => {
  const intl = useIntl();
  const { task, taskAudit } = useTaskDashboard();
  const [processes, setProcesses] = React.useState<ProcessEntry[]>([]);

  React.useEffect(() => {
    if (!taskAudit.flow) {
      return;
    }

    const processEntries: ProcessEntry[] = Object.entries(taskAudit.flow.processInstance ?? {})
      .map(([type, value]) => {
        return { 
          type: type,
          value: (datetypes.includes(type) ? undefined : value) as string, 
          targetDate: (datetypes.includes(type) ? value : undefined) as string
        };
      });
    setProcesses(processEntries);

  }, [taskAudit, task.id]);



  const columns: ColumnDef<ProcessEntry, any>[] = [
    {
      header: intl.formatMessage({ id: 'task.audit.process.type', defaultMessage: 'Type' }),
      accessorKey: 'type',
      size: 300,
      minSize: 300,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({ id: 'task.audit.process.value', defaultMessage: 'Value' }),
      accessorKey: 'value',
      size: 400,
      minSize: 400,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({ id: 'task.audit.process.createdAt', defaultMessage: 'Created' }),
      accessorKey: 'targetDate',
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
      {processes.length === 0 ? (intl.formatMessage({ id: 'task.audit.processes.None', defaultMessage: 'No processes for this task' })
      ) : (
        <WithTableStyles data={processes} columns={columns} options={{ tableId: 'taskAuditProcess', initialPageSize: 20 }} />)
      }

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