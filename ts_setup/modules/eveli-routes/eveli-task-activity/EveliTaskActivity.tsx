import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Link, Box, Typography } from '@mui/material';
import { OpenInNewOutlined as OpenInNewOutlinedIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';

import { useFetch } from '@dxs-ts/envir-fetch';
import { useQuery } from '@tanstack/react-query';
import { DateTime } from 'luxon';
import { useNavigate } from '@tanstack/react-router';
import { Editor } from '@monaco-editor/react';
import YAML from 'yaml';
import { ColumnDef, flexRender } from '@tanstack/react-table';

import { WithTableStyles } from '@dxs-ts/xui-table';
import { EveliSpinner } from '@dxs-ts/eveli-primitives';
import { EveliHealthTaskActivity } from '@dxs-ts/eveli-api';




export const EveliTaskActivity: React.FC = () => {
  const intl = useIntl();
  const { findAllTaskActivity } = useFetch('worker/rest/api/health.GET', {});

  const { data, error, refetch, isPending } = useQuery({
    queryKey: ['health-task-activity'],
    queryFn: findAllTaskActivity
  });

  if (isPending) {
    return (<EveliSpinner />
    )
  }

  const columns: ColumnDef<EveliHealthTaskActivity, any>[] = [
    {
      header: intl.formatMessage({ id: 'task.audit.health.taskActivities.taskRef', defaultMessage: 'Task ref' }),
      accessorKey: 'taskRef',
      size: 150,
      minSize: 100,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: ({ row }) => (
        <TaskRefLink taskRef={row.original.taskRef} />
      ),
    },
    {
      header: intl.formatMessage({ id: 'task.audit.health.taskActivities.ageInDays', defaultMessage: 'Age in days' }),
      accessorKey: 'ageInDays',
      size: 150,
      minSize: 100,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: {
        enableSelection: true
      }
    },

    {
      header: intl.formatMessage({ id: 'task.audit.health.taskActivities.flowName', defaultMessage: 'Flow name' }),
      accessorKey: 'flowName',
      size: 200,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: {
        enableSelection: true
      }
    },
    {
      header: intl.formatMessage({ id: 'task.audit.health.taskActivities.formName', defaultMessage: 'Form name' }),
      accessorKey: 'formName',
      size: 200,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: {
        enableSelection: true
      }
    },
    {
      header: intl.formatMessage({ id: 'task.audit.health.userActivities.created', defaultMessage: 'Created' }),
      accessorKey: 'createdAt',
      size: 200,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (updated) => flexRender(AnyTaskDateTimeShort, { value: updated.getValue() })
    },
    {
      header: intl.formatMessage({ id: 'task.audit.health.userActivities.bodyValue', defaultMessage: 'Body value' }),
      accessorKey: 'bodyValue',
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (bodyValue) => flexRender(BodyValue, { value: bodyValue.row.original })
    },
  ]

  return (
    <Box sx={{ display: 'inline-block' }}>
      <Typography variant='h1' mb={1}>
        {intl.formatMessage({ id: 'task.audit.health.taskActivities.table.title', defaultMessage: 'Audit: Task Activities' })}
      </Typography>
      <WithTableStyles data={data ?? []} columns={columns} options={{ tableId: 'eveliTaskActivityTable' }} />
    </Box>
  );
}


const toYaml = (props: any) => {
  const doc = new YAML.Document();
  doc.contents = props;
  return doc.toString();
}


const BodyValue: React.FC<{ value: any }> = ({ value }) => {
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
        <DialogTitle>{intl.formatMessage({ id: 'task.audit.health.taskActivities.dialog.title', defaultMessage: 'Body value' })}</DialogTitle>
        <DialogContent>
          <Editor
            value={toYaml(value)}
            onChange={() => { }}
            defaultLanguage='yaml'
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>{intl.formatMessage({ id: 'button.close' })}</Button>
        </DialogActions>
      </Dialog>
    </div>);
}

const TaskRefLink: React.FC<{ taskRef: string }> = ({ taskRef }) => {
  const nav = useNavigate();

  const handleClick = (event: React.MouseEvent) => {
    event.preventDefault();
    event.stopPropagation();
    nav({
      from: '/secured/$locale/worker',
      to: '/secured/$locale/worker/tasks/$taskId',
      params: { taskId: taskRef }
    })
  }


  return (
    <Box
      sx={{
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        minWidth: 0,
      }}
    >
      <Link href="#" onClick={handleClick}>
        {taskRef}
      </Link>
    </Box>
  );
};


const AnyTaskDateTimeShort: React.FC<{ value: any }> = ({ value }) => {
  const rawDate = value;
  if (!rawDate) {
    return <div>--</div>
  }
  const dateTime = DateTime.fromISO(rawDate).setLocale('fi');
  const formatted = dateTime.toLocaleString(DateTime.DATETIME_SHORT_WITH_SECONDS);

  return <div>{formatted}</div>;

}