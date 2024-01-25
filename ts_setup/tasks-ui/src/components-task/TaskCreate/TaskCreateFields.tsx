import React from 'react';
import { TextField } from '@mui/material';
import Client from 'client';
import Context from 'context';

import TaskAssignees from '../TaskAssignees';
import TaskStatus from '../TaskStatus';
import TaskPriority from '../TaskPriority';
import TaskRoles from '../TaskRoles';
import TaskStartDate from '../TaskStartDate';
import TaskDueDate from '../TaskDueDate';

const Title: React.FC<{}> = () => {
  const ctx = Context.useTaskEdit();

  function handleTitleChange(event: React.ChangeEvent<HTMLInputElement>) {
    ctx.withTask({ ...ctx.task.entry, title: event.target.value });
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    fullWidth
    value={ctx.task.title}
    onChange={handleTitleChange}
  />);
}

const Description: React.FC<{}> = () => {
  const ctx = Context.useTaskEdit();

  function handleDescriptionChange(event: React.ChangeEvent<HTMLInputElement>) {
    ctx.withTask({ ...ctx.task.entry, description: event.target.value });
  }

  return (<TextField InputProps={{ disableUnderline: true }}
    variant='standard'
    fullWidth
    multiline
    minRows={2} maxRows={4}
    value={ctx.task.description}
    onChange={handleDescriptionChange}
  />);
}

const Status: React.FC<{}> = () => {
  const ctx = Context.useTaskEdit();

  async function handleStatusChange(command: Client.ChangeTaskStatus) {
    ctx.withTask({ ...ctx.task.entry, status: command.status });
  }
  return (<TaskStatus task={ctx.task} onChange={handleStatusChange} />)
}

const Priority: React.FC<{}> = () => {
  const ctx = Context.useTaskEdit();

  async function handlePriorityChange(command: Client.ChangeTaskPriority) {
    ctx.withTask({ ...ctx.task.entry, priority: command.priority });
  }

  return (<TaskPriority task={ctx.task} priorityTextEnabled={true} onChange={handlePriorityChange} />)
}

const Assignees: React.FC<{}> = () => {
  const ctx = Context.useTaskEdit();

  async function handleAssigneeChange(assigneeIds: Client.UserId[]) {
    const command: Client.AssignTask = { assigneeIds, commandType: 'AssignTask', taskId: ctx.task.id };
    ctx.withTask({ ...ctx.task.entry, assigneeIds: command.assigneeIds });
  }

  return (<TaskAssignees task={ctx.task} onChange={handleAssigneeChange} fullnames />)
}

const Roles: React.FC<{}> = () => {
  const ctx = Context.useTaskEdit();

  async function handleRolesChange(command: Client.AssignTaskRoles) {
    ctx.withTask({ ...ctx.task.entry, roles: command.roles })
  }

  return (<TaskRoles task={ctx.task} onChange={handleRolesChange} fullnames />)
}


const StartDate: React.FC = () => {
  const ctx = Context.useTaskEdit();

  async function handleDateChange(command: Client.ChangeTaskStartDate) {
    ctx.withTask({ ...ctx.task.entry, startDate: command.startDate })
  }
  return (<TaskStartDate onChange={handleDateChange} task={ctx.task} />);
}


const DueDate: React.FC = () => {
  const ctx = Context.useTaskEdit();

  async function handleDateChange(dueDate: string | undefined) {
    const command: Client.ChangeTaskDueDate = {
      commandType: 'ChangeTaskDueDate',
      dueDate,
      taskId: ctx.task.id
    };
    ctx.withTask({ ...ctx.task.entry, dueDate: command.dueDate })
  }
  return (<TaskDueDate onChange={handleDateChange} task={ctx.task} />);
}


const Fields = { Title, Description, Status, Assignees, Roles, Priority, StartDate, DueDate };
export default Fields;
