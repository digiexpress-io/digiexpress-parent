import React from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import { ColumnDef, flexRender } from '@tanstack/react-table';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';
import Editor from '@monaco-editor/react';
import YAML from 'yaml';

import { TaskApi } from '@dxs-ts/task-api';
import { WithTableStyles } from '@dxs-ts/xui-table';
import { useTaskDashboard } from '../task-dashboard';


export const TaskAuditQueueMessagesTable: React.FC = () => {
  const intl = useIntl();
  const { task, taskAudit } = useTaskDashboard();
  const [ messages, setMessages] = React.useState<TaskApi.TaskAuditEntryMq[]>([]);

  React.useEffect(() => {

    if (taskAudit.mq?.messages) {
        setMessages(Object.values(taskAudit.mq.messages));
      } else {
        console.log("oops, no messages!")
      }

  }, [taskAudit, task.id]);

console.log(taskAudit)

  const columns: ColumnDef<TaskApi.TaskAuditEntryMq, any>[] = [
    {
      header: intl.formatMessage({ id: 'task.audit.queueMessages.routingKey', defaultMessage: 'Author' }),
      accessorKey: 'routingKey',
      size: 150,
      minSize: 100,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({ id: 'task.audit.queueMessages.bodyType', defaultMessage: 'Body type' }),
      accessorKey: 'bodyType',
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({ id: 'task.audit.queueMessages.bodyValue', defaultMessage: 'Body value' }),
      accessorKey: 'bodyValue',
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      //cell: (bodyValue) => flexRender(BodyValue, { value: bodyValue.row.original })
    },
    {
      header: intl.formatMessage({ id: 'task.audit.queueMessages.createdAt', defaultMessage: 'Created' }),
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
      <WithTableStyles data={messages} columns={columns} options={{ tableId: 'taskAuditQueueMessages' }} />
    </Box>
  );
}


const toYaml = (props: any) => {
  const doc = new YAML.Document();
  doc.contents = props;
  return doc.toString();
}

/*
const BodyValue: React.FC<{ value: TaskApi.TaskAuditEntryMq }> = ({ value }) => {
  const intl = useIntl();
  const { taskAudit } = useTaskDashboard();
  const tree = Object.values(taskAudit.mq?.messages ?? {}).find(tree => tree.commitId === value.commitId);
  const [open, setOpen] = React.useState(false);

  function handleOnClick(e: React.MouseEvent) {
    e.preventDefault();
    setOpen(true);
  }
  return (
    <div>
      <a href='#' onClick={handleOnClick}>...</a>
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
    */
const AnyTaskDateTimeShort: React.FC<{ value: any }> = ({ value }) => {
  const rawDate = value;
  if (!rawDate) {
    return <div>--</div>
  }
  const dateTime = DateTime.fromISO(rawDate).setLocale('fi');
  const formatted = dateTime.toLocaleString(DateTime.DATETIME_SHORT_WITH_SECONDS);

  return <div>{formatted}</div>;
}