import React from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import { OpenInNewOutlined as OpenInNewOutlinedIcon } from '@mui/icons-material';

import { ColumnDef, flexRender } from '@tanstack/react-table';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';
import Editor from '@monaco-editor/react';
import YAML from 'yaml';

import { TaskApi } from '@dxs-ts/task-api';
import { WithTableStyles } from '@dxs-ts/xui-table';
import { useTaskDashboard } from '../task-dashboard';


export const TaskAuditCommitsTable: React.FC = () => {
  const intl = useIntl();
  const { task, taskAudit } = useTaskDashboard();
  const [commits, setCommits] = React.useState<TaskApi.TaskCommit[]>([]);

  React.useEffect(() => {

    if (taskAudit.access.commits) {
      const sortedCommits = Object.values(taskAudit.access.commits)
          .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        setCommits(sortedCommits);
      } else {
        console.log("oops, no commits!")
      }

  }, [taskAudit, task.id]);



  const columns: ColumnDef<TaskApi.TaskCommit, any>[] = [
    {
      header: intl.formatMessage({ id: 'task.audit.commits.author', defaultMessage: 'Author' }),
      accessorKey: 'commitAuthor',
      size: 200,
      minSize: 200,
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
      header: intl.formatMessage({ id: 'task.audit.commits.commitBody', defaultMessage: 'Body' }),
      accessorKey: 'commitBody',
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (updated) => flexRender(CommitBody, { value: updated.row.original })
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


const toYaml = (props: any) => {
  const doc = new YAML.Document();
  doc.contents = props;
  return doc.toString();
}

const CommitBody: React.FC<{ value: TaskApi.TaskCommit }> = ({ value }) => {
  const intl = useIntl();
  const { taskAudit } = useTaskDashboard();
  const tree = Object.values(taskAudit.access.commitTrees).find(tree => tree.commitId === value.commitId);
  const [open, setOpen] = React.useState(false);

  function handleOnClick(e: React.MouseEvent) {
    e.preventDefault();
    setOpen(true);
  }
  return (
    <div>
      <Button endIcon={<OpenInNewOutlinedIcon />} variant='text' sx={{ fontSize: '9pt' }} onClick={handleOnClick}>
        {intl.formatMessage({ id: 'button.view', defaultMessage: 'View' })}
      </Button>
      <Dialog fullScreen open={open} onClose={() => setOpen(false)}>
        <DialogTitle>{intl.formatMessage({ id: 'task.audit.commits.commitBody.title', defaultMessage: 'Commit body' })}</DialogTitle>
        <DialogContent>
          <Editor
            value={toYaml(tree)}
            onChange={() => { }}
            defaultLanguage='yaml'
            height='500px'
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>{intl.formatMessage({ id: 'button.close' })}</Button>
        </DialogActions>
      </Dialog>
    </div>);
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