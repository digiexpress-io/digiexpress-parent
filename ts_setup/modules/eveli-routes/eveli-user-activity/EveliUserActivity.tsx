
import React from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Link, Typography } from '@mui/material';
import OpenInNewOutlinedIcon from '@mui/icons-material/OpenInNewOutlined';
import { Editor } from '@monaco-editor/react';
import { ColumnDef, flexRender } from '@tanstack/react-table';
import { useQuery } from "@tanstack/react-query";
import { useNavigate } from '@tanstack/react-router';
import { useIntl } from 'react-intl';
import YAML from 'yaml';
import { DateTime } from 'luxon';

import { useFetch } from '@dxs-ts/envir-fetch';
import { WithTableStyles } from '@dxs-ts/xui-table';
import { EveliHealthUserActivity } from '@dxs-ts/eveli-api';
import { EveliSpinner } from '@dxs-ts/eveli-primitives';



export const EveliUserActivity: React.FC = () => {
  const intl = useIntl();

  const { findAllUserActivity } = useFetch('worker/rest/api/health.GET', {});

  const { data, error, refetch, isPending } = useQuery({
    queryKey: ['health-user-activity'],
    queryFn: findAllUserActivity
  });

  if (isPending) {
    return (<EveliSpinner />
    )
  }

  const columns: ColumnDef<EveliHealthUserActivity, any>[] = [
    {
      header: intl.formatMessage({ id: 'task.audit.health.userActivities.taskRef', defaultMessage: 'Task ref' }),
      accessorKey: 'taskRef',
      size: 150,
      minSize: 100,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: ({ row }) => (
        <TaskRefLink taskRef={row.original.taskRef} />
      ),
      meta: {
        enableSelection: true
      }
    },
    {
      header: intl.formatMessage({ id: 'task.audit.health.userActivities.userName', defaultMessage: 'Username' }),
      accessorKey: 'userName',
      size: 200,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: {
        enableSelection: true
      },
      cell: ({ row }) => {
       const { userName, usedFor } = row.original;
       return usedFor === 'WORKER' ? userName : '-'
      }
    },
    {
      header: intl.formatMessage({ id: 'task.audit.health.userActivities.usedFor', defaultMessage: 'Used for' }),
      accessorKey: 'usedFor',
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
      header: intl.formatMessage({ id: 'task.audit.health.userActivities.type', defaultMessage: 'Type' }),
      accessorKey: 'type',
      size: 150,
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
        {intl.formatMessage({ id: 'task.audit.health.userActivities.table.title', defaultMessage: 'Audit: User Activities' })}
      </Typography>
      <WithTableStyles data={data ?? []} columns={columns} options={{ tableId: 'eveliUserActivityTable' }} />
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
        <DialogTitle>{intl.formatMessage({ id: 'task.audit.health.userActivities.dialog.title', defaultMessage: 'Body value' })}</DialogTitle>
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
      to:'/secured/$locale/worker/tasks/$taskId',
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