import React from 'react';
import { Box, Typography, IconButton, Tooltip, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, Button } from '@mui/material';

import { CellContext, ColumnDef, flexRender } from '@tanstack/react-table';
import { DateTime } from 'luxon';
import AddIcon from '@mui/icons-material/Add';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';

import { useIntl, FormattedMessage } from 'react-intl';

import { WithTableStyles } from '@dxs-ts/xui-table';
import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';


import { IndicatorPriority } from './IndicatorPriority';
import { IndicatorStatus } from './IndicatorStatus';
import { IndicatorAssignee } from './IndicatorAssignee';
import { IndicatorSubject } from './IndicatorSubject';

import { filterFormattedDateFn, filterStringOrArrayFn, filterTaskRefOrSubjectFn, taskSortingFn } from './tableHelpers';



export const TaskTable: React.FC = () => {
  const intl = useIntl();
  const backend = useTaskBackend();
  const [data, setData] = React.useState<TaskApi.Task[]>([]);
  const [deleteId, setDeleteId] = React.useState<string | undefined>();

  React.useEffect(() => {
    backend.persistence.findAllTasks().then(setData);
  }, []);


  async function handleDelete(data: { id: string }) {
    backend.persistence.deleteOneTask(data.id).then(() => {
      backend.persistence.findAllTasks().then(setData);
    });
  }

  function confirmDelete(id: string) {
    setDeleteId(id);
  };

  function handleCancelDelete() {
    setDeleteId(undefined);
  };

  async function handleConfirmDelete() {
    if (deleteId) {
      await handleDelete({ id: deleteId });
      setDeleteId(undefined);
    }
  };

  const columns: ColumnDef<TaskApi.Task, any>[] = [
    {
      header: 'Priority',
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
      header: 'Name',
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
      header: 'Info',
      accessorKey: 'additionalInfo',
      size: 100,
      minSize: 100,
      enableSorting: false,
      enableResizing: true,
      enableColumnFilter: true,
    },
    {
      header: 'Client',
      accessorKey: 'clientIdentificator',
      filterFn: 'includesString',
      size: 150,
      minSize: 150,
      enableColumnFilter: true,
      enableResizing: true,
    },
    {
      header: 'Status',
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
      header: 'Roles',
      accessorKey: 'assignedRoles',
      filterFn: filterStringOrArrayFn,
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
      header: 'Assignee',
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
      header: 'Due',
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
      header: 'Created',
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
      header: 'Archive',
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
          <IconButton color='error' onClick={() => confirmDelete(row.original.id)} size="small">
            <DeleteForeverIcon color="error" fontSize="small" />
          </IconButton>
        </div>
      )
    }] : [])
  ]


  return (
    <Box sx={{ display: 'inline-block' }}>
      <Box display="flex" alignItems="center" mb={2}>
        <Typography variant="h1" sx={{ flexGrow: 1 }}>
          <FormattedMessage id="tasksView.title" />
        </Typography>
        {backend.permissions.isCreateTaskAllowed && (
          <Tooltip title={intl.formatMessage({ id: 'taskButton.addTask' })}>
            <IconButton onClick={() => backend.navigate.createOneTask()}>
              <AddIcon />
            </IconButton>
          </Tooltip>
        )}
      </Box>
      <DeleteConfirmationDialog tasks={data} deleteId={deleteId} handleCancelDelete={handleCancelDelete} handleConfirmDelete={handleConfirmDelete} />
      <WithTableStyles data={data} columns={columns} options={{ tableId: 'tasks' }} />
    </Box>
  );
}


interface DeleteConfirmationDialogProps {
  tasks: TaskApi.Task[],
  deleteId: string | undefined,
  handleCancelDelete: () => void,
  handleConfirmDelete: () => void
}


const DeleteConfirmationDialog: React.FC<DeleteConfirmationDialogProps> = ({ tasks, deleteId, handleCancelDelete, handleConfirmDelete }) => {
  const intl = useIntl();
  const task = tasks.find(t => t.id === deleteId);

  return (
    <Dialog open={!!deleteId} onClose={handleCancelDelete}>
      <DialogTitle>
        {intl.formatMessage({ id: 'task.confirmDelete.title', defaultMessage: `Confirm archive task: ${task?.taskRef ?? ''}` })}
      </DialogTitle>
      <DialogContent>
        <DialogContentText>
          {intl.formatMessage({
            id: 'taskTable.button.archive.confirm',
            defaultMessage: 'You are about to archive this task, which will remove it from the active tasks. Are you sure you want to continue?',
          })}
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleCancelDelete} variant='outlined'>
          {intl.formatMessage({ id: 'button.cancel', defaultMessage: 'Cancel' })}
        </Button>
        <Button onClick={handleConfirmDelete} color="error" autoFocus>
          {intl.formatMessage({ id: 'button.archive', defaultMessage: 'Archive' })}
        </Button>
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
