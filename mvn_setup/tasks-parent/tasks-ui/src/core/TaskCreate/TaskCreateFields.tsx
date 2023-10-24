import React from 'react';
import { TextField, Typography, Stack, Button } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';

import DateRangeOutlinedIcon from '@mui/icons-material/DateRangeOutlined';

import Client from 'taskclient';
import Context from 'context';

import TaskAssignees from '../TaskAssignees';
import TaskStatus from '../TaskStatus';
import TaskPriority from '../TaskPriority';

/*
export interface CreateTask extends TaskCommand {
  commandType: 'CreateTask';
  title: string;
  description: string;
  roles: string[];
  assigneeIds: string[];
  reporterId: string;
  priority: TaskPriority;
  labels: string[];
 
  extensions: TaskExtension[];
  comments: TaskComment[];
  checklist: Checklist[];
 
  status: TaskStatus | undefined;
  startDate: string | undefined;
  dueDate: string | undefined;
}
*/

const Title: React.FC<{}> = () => {
  const intl = useIntl();
  const { state, setState } = Context.useTaskEdit();

  function handleTitleChange(event: React.ChangeEvent<HTMLInputElement>) {
    setState((current) => current.withTask({ ...state.task.entry, title: event.target.value }));
  }

  return (<TextField placeholder={intl.formatMessage({ id: 'core.taskCreate.taskTitle' })}
    fullWidth
    InputProps={{ sx: { fontSize: '20pt' } }}
    value={state.task.title}
    onChange={handleTitleChange}
  />);
}

const Description: React.FC<{}> = () => {
  const { state, setState } = Context.useTaskEdit();
  const intl = useIntl();

  function handleDescriptionChange(event: React.ChangeEvent<HTMLInputElement>) {
    setState((current) => current.withTask({ ...state.task.entry, description: event.target.value }));
  }

  return (<TextField
    placeholder={intl.formatMessage({ id: 'core.taskEdit.taskDescription' })}
    fullWidth multiline minRows={4} maxRows={6}
    value={state.task.description}
    onChange={handleDescriptionChange}
  />);
}

const Status: React.FC<{}> = () => {
  const { state, setState } = Context.useTaskEdit();

  async function handleStatusChange(command: Client.ChangeTaskStatus) {
    setState((current) => current.withTask({ ...state.task.entry, status: command.status }));
  }
  return (
    <TaskStatus task={state.task} onChange={handleStatusChange} />
  )
}

const Priority: React.FC<{}> = () => {
  const { state, setState } = Context.useTaskEdit();
  const backend = Context.useBackend();

  async function handlePriorityChange(command: Client.ChangeTaskPriority) {
    const updatedTask = await backend.task.updateActiveTask(state.task.id, [command]);
    setState((current) => current.withTask(updatedTask));
  }

  return (
    <TaskPriority task={state.task} priorityTextEnabled onChange={handlePriorityChange} />
  )
}

const Assignee: React.FC<{}> = () => {
  const { state, setState } = Context.useTaskEdit();
  const backend = Context.useBackend();

  async function handleAssigneeChange(command: Client.AssignTask) {
    const updatedTask = await backend.task.updateActiveTask(state.task.id, [command]);
    setState((current) => current.withTask(updatedTask));
  }

  return (
    <TaskAssignees task={state.task} onChange={handleAssigneeChange} />
  )
}

const StartDate: React.FC<{ onClick: () => void }> = ({ onClick }) => {
  const { state } = Context.useTaskEdit();
  const startDate = state.task.startDate;

  return (
    <Stack spacing={1} direction='row' alignItems='center'>
      <Typography variant='body2'><FormattedMessage id='core.taskWork.startDate' /></Typography>
      <Button onClick={onClick}>{startDate ? <Typography>{startDate.toLocaleDateString()}</Typography> : <DateRangeOutlinedIcon />}</Button>
    </Stack>
  );
}

const DueDate: React.FC<{ onClick: () => void }> = ({ onClick }) => {
  const { state } = Context.useTaskEdit();
  const dueDate = state.task.dueDate;

  return (
    <Stack spacing={1} direction='row' alignItems='center'>
      <Typography variant='body2'><FormattedMessage id='core.taskWork.dueDate' /></Typography>
      <Button onClick={onClick}>{dueDate ? <Typography>{dueDate.toLocaleDateString()}</Typography> : <DateRangeOutlinedIcon />}</Button>
    </Stack>
  );
}


const Fields = { Title, Description, Status, Assignee, Priority, StartDate, DueDate };
export default Fields;
