import React from 'react';
import { TextField } from '@mui/material';
import Client from 'taskclient';
import Context from 'context';

import TaskAssignees from '../TaskAssignees';
import TaskStatus from '../TaskStatus';
import TaskPriority from '../TaskPriority';
import TaskRoles from '../TaskRoles';
import TaskStartDate from '../TaskStartDate';
import TaskDueDate from '../TaskDueDate';

const Title: React.FC<{}> = () => {
  const { state, setState } = Context.useTaskEdit();

  function handleTitleChange(event: React.ChangeEvent<HTMLInputElement>) {
    setState((current) => current.withTask({ ...state.task.entry, title: event.target.value }));
  }

  return (<TextField InputProps={{ disableUnderline: true }} variant='standard'
    fullWidth
    value={state.task.title}
    onChange={handleTitleChange}
  />);
}

const Description: React.FC<{}> = () => {
  const { state, setState } = Context.useTaskEdit();

  function handleDescriptionChange(event: React.ChangeEvent<HTMLInputElement>) {
    setState((current) => current.withTask({ ...state.task.entry, description: event.target.value }));
  }

  return (<TextField InputProps={{ disableUnderline: true }}
    variant='standard'
    fullWidth
    multiline
    minRows={2} maxRows={4}
    value={state.task.description}
    onChange={handleDescriptionChange}
  />);
}

const Status: React.FC<{}> = () => {
  const { state, setState } = Context.useTaskEdit();

  async function handleStatusChange(command: Client.ChangeTaskStatus) {
    setState((current) => current.withTask({ ...state.task.entry, status: command.status }));
  }
  return (<TaskStatus task={state.task} onChange={handleStatusChange} />)
}

const Priority: React.FC<{}> = () => {
  const { state, setState } = Context.useTaskEdit();

  async function handlePriorityChange(command: Client.ChangeTaskPriority) {
    setState((current) => current.withTask({ ...state.task.entry, priority: command.priority }));
  }

  return (<TaskPriority task={state.task} priorityTextEnabled={true} onChange={handlePriorityChange} />)
}

const Assignees: React.FC<{}> = () => {
  const { state, setState } = Context.useTaskEdit();

  async function handleAssigneeChange(command: Client.AssignTask) {
    setState((current) => current.withTask({ ...state.task.entry, assigneeIds: command.assigneeIds }));
  }

  return (<TaskAssignees task={state.task} onChange={handleAssigneeChange} fullnames />)
}

const Roles: React.FC<{}> = () => {
  const { state, setState } = Context.useTaskEdit();

  async function handleRolesChange(command: Client.AssignTaskRoles) {
    setState((current) => current.withTask({ ...state.task.entry, roles: command.roles }))
  }

  return (<TaskRoles task={state.task} onChange={handleRolesChange} fullnames />)
}


const StartDate: React.FC = () => {
  const { state, setState } = Context.useTaskEdit();

  async function handleDateChange(command: Client.ChangeTaskStartDate) {
    setState((current) => current.withTask({ ...state.task.entry, startDate: command.startDate }))
  }
  return (<TaskStartDate onChange={handleDateChange} task={state.task} />);
}


const DueDate: React.FC = () => {
  const { state, setState } = Context.useTaskEdit();

  async function handleDateChange(command: Client.ChangeTaskDueDate) {
    setState((current) => current.withTask({ ...state.task.entry, dueDate: command.dueDate }))
  }
  return (<TaskDueDate onChange={handleDateChange} task={state.task} />);
}


const Fields = { Title, Description, Status, Assignees, Roles, Priority, StartDate, DueDate };
export default Fields;
