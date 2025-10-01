import React from 'react';
import { Box, Typography, IconButton, Tooltip, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, Button } from '@mui/material';

import { CellContext, ColumnDef, flexRender } from '@tanstack/react-table';
import { DateTime } from 'luxon';
import AddIcon from '@mui/icons-material/Add';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';

import { useIntl, FormattedMessage } from 'react-intl';
import { useQuery } from '@tanstack/react-query';
import { WithTableStyles } from '@dxs-ts/xui-table';
import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';


import { IndicatorPriority } from './IndicatorPriority';
import { IndicatorStatus } from './IndicatorStatus';
import { IndicatorAssignee } from './IndicatorAssignee';
import { IndicatorSubject } from './IndicatorSubject';

import { filterFormattedDateFn, filterStringOrArrayFn, filterTaskRefOrSubjectFn, taskSortingFn } from './tableHelpers';
import { IndicatorRole } from './IndicatorRole';

export const TASK_TABLE_QUERY_KEY = 'find-all-tasks';

export const TaskTable: React.FC = () => {
  const intl = useIntl();
  const backend = useTaskBackend();
  const [deleteId, setArchiveId] = React.useState<string | undefined>();

  const { data, error, refetch, isPending } = useQuery({
    queryKey: [TASK_TABLE_QUERY_KEY],
    queryFn: () => backend.persistence.findAllTasks(),
    initialData: [],
  });

  async function handleArchive(data: { id: string }) {
    backend.persistence.deleteOneTask(data.id).then(() => refetch());
  }

  function confirmArchive(id: string) {
    setArchiveId(id);
  };

  function handleCancelArchive() {
    setArchiveId(undefined);
  };

  async function handleConfirmArchive() {
    if (deleteId) {
      await handleArchive({ id: deleteId });
      setArchiveId(undefined);
    }
  };

  const columns: ColumnDef<TaskApi.Task, any>[] = [
    {
      header: intl.formatMessage({ id: 'taskTable.col.header.priority', defaultMessage: 'Priority' }),
      accessorKey: 'priority',
      filterFn: filterStringOrArrayFn,
      sortingFn: taskSortingFn,
      cell: (priority) => flexRender(IndicatorPriority, { type: priority.getValue() }),
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
      header: intl.formatMessage({ id: 'taskTable.col.header.subject', defaultMessage: 'Subject' }),
      accessorKey: 'subject',
      sortingFn: taskSortingFn,
      cell: (task) => flexRender(IndicatorSubject, { title: task.getValue(), id: task.row.original.id, keywords: task.row.original.keyWords }),
      filterFn: filterTaskRefOrSubjectFn,
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableHiding: true,
      enableResizing: true,
      meta: {
        enableSelection: true
      }
    },
    {
      header: intl.formatMessage({ id: 'taskTable.col.header.addInfo', defaultMessage: 'Info' }),
      accessorKey: 'additionalInfo',
      size: 100,
      minSize: 100,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
    },
    {
      header: intl.formatMessage({ id: 'taskTable.col.header.client', defaultMessage: 'Client' }),
      accessorKey: 'clientIdentificator',
      filterFn: 'includesString',
      size: 150,
      minSize: 150,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: intl.formatMessage({ id: 'taskTable.col.header.status', defaultMessage: 'Status' }),
      accessorKey: 'status',
      filterFn: filterStringOrArrayFn,
      size: 150,
      minSize: 150,
      cell: (status) => flexRender(IndicatorStatus, { status: status.getValue() }),
      sortingFn: taskSortingFn,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: {
        enableSelection: true
      }
    },
    {
      header: intl.formatMessage({ id: 'taskTable.col.header.roles', defaultMessage: 'Roles' }),
      accessorKey: 'assignedRoles',
      filterFn: filterStringOrArrayFn,
      size: 150,
      minSize: 150,
      cell: (assignedRoles) => flexRender(IndicatorRole, { taskRoles: assignedRoles.getValue(), roles: backend.roles }),
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: {
        enableSelection: true
      }
    },
    {
      header: intl.formatMessage({ id: 'taskTable.col.header.assignee', defaultMessage: 'Assignee' }),
      accessorKey: 'assignedUser',
      filterFn: filterStringOrArrayFn,
      cell: (assignee) => flexRender(IndicatorAssignee, { name: assignee.getValue() }),
      sortingFn: taskSortingFn,
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
      header: intl.formatMessage({ id: 'taskTable.col.header.due', defaultMessage: 'Due' }),
      accessorKey: 'dueDate',
      filterFn: filterFormattedDateFn,
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (dueDate) => flexRender(AnyTaskDateTimeShort, { value: dueDate.getValue() })
    },
    {
      header: intl.formatMessage({ id: 'taskTable.col.header.created', defaultMessage: 'Created' }),
      accessorKey: 'created',
      filterFn: filterFormattedDateFn,
      size: 150,
      minSize: 150,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      cell: (created) => flexRender(AnyTaskDateTimeShort, { value: created.getValue() })
    },
    ...(backend.permissions.isDeleteTaskAllowed ? [{
      header: intl.formatMessage({ id: 'taskTable.col.header.archive', defaultMessage: 'Archive' }),
      filterFn: filterFormattedDateFn,
      size: 100,
      minSize: 100,
      enableSorting: true,
      enableColumnFilter: true,
      enableResizing: true,
      meta: {
        enableSelection: true
      },
      cell: ({ row }: CellContext<TaskApi.Task, unknown>) => (
        <div style={{ display: 'flex', justifyContent: 'center' }}>
          <IconButton color='error' onClick={() => confirmArchive(row.original.id)} size="small">
            <DeleteForeverIcon color="error" fontSize="small" />
          </IconButton>
        </div>
      )
    }] : [])
  ]


  return (
    <>
      <Box display="flex" alignItems="center" mb={2}>
        <Typography variant="h1" sx={{ flexGrow: 1 }}>{intl.formatMessage({ id: 'taskTable.title', defaultMessage: 'Tasks' })}</Typography>
        {backend.permissions.isCreateTaskAllowed && (
          <Tooltip title={intl.formatMessage({ id: 'taskButton.addTask' })}>
            <IconButton onClick={() => backend.navigate.createOneTask()}>
              <AddIcon />
            </IconButton>
          </Tooltip>
        )}
      </Box>
      <ArchiveConfirmationDialog tasks={data} deleteId={deleteId} handleCancelArchive={handleCancelArchive} handleConfirmArchive={handleConfirmArchive} />
      <WithTableStyles data={data} columns={columns} options={{ tableId: 'tasks' }} />
    </>
  );
}


interface ArchiveConfirmationDialogProps {
  tasks: TaskApi.Task[],
  deleteId: string | undefined,
  handleCancelArchive: () => void,
  handleConfirmArchive: () => void
}


const ArchiveConfirmationDialog: React.FC<ArchiveConfirmationDialogProps> = ({ tasks, deleteId, handleCancelArchive, handleConfirmArchive }) => {
  const intl = useIntl();
  const task = tasks.find(t => t.id === deleteId);

  return (
    <Dialog open={!!deleteId} onClose={handleCancelArchive}>
      <DialogTitle>{intl.formatMessage({ id: 'task.confirmArchive.title', defaultMessage: `Confirm archive task: ${task?.taskRef ?? ''}` })}</DialogTitle>
      <DialogContent>
        <DialogContentText>
          {intl.formatMessage({
            id: 'taskTable.button.archive.confirm',
            defaultMessage: 'You are about to archive this task, which will remove it from the active tasks. Are you sure you want to continue?',
          })}
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleCancelArchive} variant='outlined'>{intl.formatMessage({ id: 'button.cancel', defaultMessage: 'Cancel' })}</Button>
        <Button onClick={handleConfirmArchive} color="error" autoFocus>{intl.formatMessage({ id: 'button.archive', defaultMessage: 'Archive' })}</Button>
      </DialogActions>
    </Dialog>
  )
}

const AnyTaskDateTimeShort: React.FC<{ value: any }> = ({ value }) => {
  const rawDate = value;
  if (!rawDate) {
    return <div>--</div>
  }
  const dateTime = DateTime.fromISO(rawDate).setLocale('fi');
  const formatted = dateTime.toLocaleString(DateTime.DATE_SHORT);

  return <div>{formatted}</div>;
}
