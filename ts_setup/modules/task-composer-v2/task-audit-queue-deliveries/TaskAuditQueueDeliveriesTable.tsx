import React from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import OpenInNewOutlinedIcon from '@mui/icons-material/OpenInNewOutlined';

import { ColumnDef, flexRender } from '@tanstack/react-table';
import { DateTime } from 'luxon';
import { useIntl } from 'react-intl';

import { TaskApi } from '@dxs-ts/task-api';
import { WithTableStyles } from '@dxs-ts/xui-table';
import { useTaskDashboard } from '../task-dashboard';
import { Editor } from '@monaco-editor/react';
import YAML from 'yaml';


export const TaskAuditQueueDeliveriesTable: React.FC = () => {
  const intl = useIntl();
  const { task, taskAudit } = useTaskDashboard();
  const [ deliveries, setDeliveries] = React.useState<TaskApi.TaskAuditQueueDelivery[]>([]);

  React.useEffect(() => {
    if (taskAudit.mq?.deliveries) {
      setDeliveries(Object.values(taskAudit.mq.deliveries));
      } else {
        console.log("oops, no deliveries!")
      }
  }, [taskAudit, task.id]);


  const columns: ColumnDef<TaskApi.TaskAuditQueueDelivery, any>[] = [
    {
      header: intl.formatMessage({ id: 'task.audit.queueDeliveries.queueName', defaultMessage: 'Queue name' }),
      accessorKey: 'queueId',
      size: 300,
      minSize: 200,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (attempts) => flexRender(QueueName, { value: attempts.row.original.queueId })
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
      header: intl.formatMessage({ id: 'task.audit.queueBindings.attempts', defaultMessage: 'Attempts' }),
      accessorKey: 'attempts',
      size: 150,
      minSize: 100,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (attempts) => flexRender(Attempts, { value: attempts.row.original })
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
      <WithTableStyles data={deliveries} columns={columns} options={{ tableId: 'taskAuditQueueDeliveries' }} />
    </Box>
  );
}


const toYaml = (props: any) => {
  const doc = new YAML.Document();
  doc.contents = props;
  return doc.toString();
}





const QueueName: React.FC<{ value: string }> = ({ value }) => {
  const { taskAudit } = useTaskDashboard();

  console.log(taskAudit.mq?.queues[value]);
  return (<>{taskAudit.mq?.queues[value].queueName}</>)
}

const Attempts: React.FC<{ value: any }> = ({ value }) => {
  const intl = useIntl();
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
        <DialogTitle>{intl.formatMessage({ id: 'task.audit.deliveries.attempts.title', defaultMessage: 'Attempts' })}</DialogTitle>
        <DialogContent>
          <Editor
            value={toYaml(value)}
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