import React from 'react';
import { ImmutableCustomerStore } from 'descriptor-customer';
import { useBackend } from 'descriptor-backend';
import { XTableHead, XPaper, XPaperTitleTypography, XTable, XTableBody, XTableBodyCell, XTableHeader, XTableRow } from 'components-xtable';
import { AssignTask, AssignTaskRoles, ChangeTaskDueDate, ChangeTaskPriority, ChangeTaskStatus, ImmutableTaskDescriptor, TaskDescriptor, useTasks } from 'descriptor-task';
import { PrincipalId, useAm } from 'descriptor-access-mgmt';
import { Button } from '@mui/material';
import { useToggle } from 'components-generic';
import { TaskAssignees, TaskEditDialog, TaskPriority, TaskRoles, TaskStatus } from 'components-task';
import Table from 'table';
import { FormattedMessage } from 'react-intl';
import { TaskDueDate } from 'components-task';

type TaskPagination = Table.TablePagination<TaskDescriptor>;

function initTable(): TaskPagination {
  return new Table.TablePaginationImpl<TaskDescriptor>({
    src: [],
    orderBy: 'created',
    order: 'asc',
    sorted: true,
    rowsPerPage: 100,
  })
}


export const CustomerTaskTitle: React.FC<{ task: TaskDescriptor }> = ({ task }) => {
  const editTask = useToggle();
  return (
    <>
      <TaskEditDialog open={editTask.open} onClose={editTask.handleEnd} task={task} />
      <Button variant='text' onClick={editTask.handleStart}>
        {task.title}
      </Button>
    </>
  );
}


export const CustomerTasks: React.FC<{ customerId: string }> = ({ customerId }) => {
  const [loading, setLoading] = React.useState(false);
  const [tasks, setTasks] = React.useState<TaskPagination>(initTable());
  const backend = useBackend();
  const am = useAm();
  const tasksCtx = useTasks();

  React.useEffect(() => {
    setLoading(true);
    new ImmutableCustomerStore(backend.store).findCustomerTasks(customerId).then(newRecords => {
      const today = new Date();
      setTasks(prev => prev.withRowsPerPage(newRecords.length).withSrc(newRecords.map(t => new ImmutableTaskDescriptor(t, am.profile, today))));
      setLoading(false);
      return;
    });

  }, [customerId]);


  async function handleAssignTask(task: TaskDescriptor, assigneeIds: PrincipalId[]) {
    const command: AssignTask = { assigneeIds, commandType: 'AssignTask', taskId: task.id };
    await tasksCtx.updateActiveTask(task.id, [command]);
  }
  async function handleDueDateChange(task: TaskDescriptor, dueDate: string | undefined) {
    const command: ChangeTaskDueDate = { commandType: 'ChangeTaskDueDate', dueDate, taskId: task.id };
    await tasksCtx.updateActiveTask(task.id, [command]);
  }
  async function handlePriorityChange(task: TaskDescriptor, command: ChangeTaskPriority) {
    await tasksCtx.updateActiveTask(task.id, [command]);
  }
  async function handleAssignRolesChange(task: TaskDescriptor, command: AssignTaskRoles) {
    await tasksCtx.updateActiveTask(task.id, [command]);
  }
  async function handleStatusChange(task: TaskDescriptor, command: ChangeTaskStatus) {
    await tasksCtx.updateActiveTask(task.id, [command]);
  }

  function handleSorting(key: string, _direction: string) {
    setTasks(prev => prev.withOrderBy(key as keyof TaskDescriptor));
  }

  if (loading) {
    return (<></>);
  }

  return (
    <>
        <XTable columns={6} rows={tasks.rowsPerPage}>
          <XTableHead>
            <XTableRow>
              <XTableHeader onSort={handleSorting} sortable id='title'><FormattedMessage id='tasktable.header.title' /></XTableHeader>
              <XTableHeader onSort={handleSorting} sortable id='assignees'><FormattedMessage id='tasktable.header.assignees' /></XTableHeader>
              <XTableHeader onSort={handleSorting} sortable id='dueDate' defaultSort='asc'><FormattedMessage id='tasktable.header.dueDate' /></XTableHeader>
              <XTableHeader onSort={handleSorting} sortable id='priority'><FormattedMessage id='tasktable.header.priority' /></XTableHeader>
              <XTableHeader onSort={handleSorting} sortable id='roles'><FormattedMessage id='tasktable.header.roles' /></XTableHeader>
              <XTableHeader onSort={handleSorting} sortable id='status'><FormattedMessage id='tasktable.header.status' /></XTableHeader>
            </XTableRow>
          </XTableHead>
          <XTableBody>
            {tasks.entries.map((row) => (
              <XTableRow key={row.id}>
                <XTableBodyCell id="title" justifyContent='left' maxWidth={"500px"}><CustomerTaskTitle task={row}/></XTableBodyCell>
                <XTableBodyCell id="assignees"><TaskAssignees task={row} onChange={(assigneeIds) => handleAssignTask(row, assigneeIds)} /></XTableBodyCell>
                <XTableBodyCell id="dueDate"><TaskDueDate task={row} onChange={(dueDate) => handleDueDateChange(row, dueDate)} /></XTableBodyCell>
                <XTableBodyCell id="priority"><TaskPriority task={row} onChange={(priority) => handlePriorityChange(row, priority)} /></XTableBodyCell>
                <XTableBodyCell id="roles"><TaskRoles task={row} onChange={(roles) => handleAssignRolesChange(row, roles)} /></XTableBodyCell>
                <XTableBodyCell id="status" width="100px"><TaskStatus task={row} onChange={(status) => handleStatusChange(row, status)} /></XTableBodyCell>
              </XTableRow>))
            }
          </XTableBody>
        </XTable>

    </>);
}