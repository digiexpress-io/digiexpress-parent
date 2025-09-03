import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { WithTableStyles } from '@dxs-ts/xui-table';
import { Box } from '@mui/system';
import { ColumnDef, flexRender } from '@tanstack/react-table';
import { DateTime } from 'luxon';
import React from 'react';
import { useIntl } from 'react-intl';
import { useTaskDashboard } from '../task-dashboard';


export const TaskAuditCommitsTable: React.FC = () => {
  const intl = useIntl();
  const backend = useTaskBackend();
  const { task } = useTaskDashboard();
  const [commits, setCommits] = React.useState<TaskApi.TaskCommit[]>([]);

  React.useEffect(() => {
    backend.persistence.getOneTaskAudit(task.id).then((audit) => {
      if (audit.access.commits) {
        const sortedCommits = Object.values(audit.access.commits)
          .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        setCommits(sortedCommits);
      } else {
        console.log("oops, no commits!")
      }
    });
  }, [backend, task.id]);


  console.log("Commits", commits)
  const columns: ColumnDef<TaskApi.TaskCommit, any>[] = [
    {
      header: intl.formatMessage({ id: 'task.audit.commits.author', defaultMessage: 'Author' }),
      accessorKey: 'commitAuthor',
      size: 300,
      minSize: 300,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({ id: 'task.audit.commits.message', defaultMessage: 'Message' }),
      accessorKey: 'commitMessage',
      size: 400,
      minSize: 400,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({ id: 'task.audit.commits.createdAt', defaultMessage: 'Created' }),
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
      <WithTableStyles data={commits} columns={columns} options={{ tableId: 'taskAuditCommits' }} />
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