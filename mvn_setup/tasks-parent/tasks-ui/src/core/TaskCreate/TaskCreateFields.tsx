import React from 'react';
import { TextField, Typography, Stack, Button, Box } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';

import DateRangeOutlinedIcon from '@mui/icons-material/DateRangeOutlined';

import Client from 'taskclient';
import Context from 'context';

import TaskAssignees from '../TaskAssignees';
import TaskStatus from '../TaskStatus';
import TaskPriority from '../TaskPriority';
import TaskRoles from '../TaskRoles';




const Title: React.FC<{}> = () => {
  const intl = useIntl();
  const { state, setState } = Context.useTaskEdit();

  function handleTitleChange(event: React.ChangeEvent<HTMLInputElement>) {
    setState((current) => current.withTask({ ...state.task.entry, title: event.target.value }));
  }

  return (<TextField placeholder={intl.formatMessage({ id: 'core.taskCreate.taskTitle' })}
    fullWidth
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
  return (<Box display='flex' alignItems='center'>
    <Typography><FormattedMessage id='core.taskCreate.fields.status' /></Typography>
    <TaskStatus task={state.task} onChange={handleStatusChange} />
  </Box>
  )
}

const Priority: React.FC<{}> = () => {
  const { state, setState } = Context.useTaskEdit();

  async function handlePriorityChange(command: Client.ChangeTaskPriority) {
    setState((current) => current.withTask({ ...state.task.entry, priority: command.priority }));
  }

  return (<Box display='flex' alignItems='center'>
    <Typography><FormattedMessage id='core.taskCreate.fields.priority' /></Typography>
    <TaskPriority task={state.task} priorityTextEnabled={true} onChange={handlePriorityChange} />
  </Box>
  )
}

const Assignees: React.FC<{}> = () => {
  const { state, setState } = Context.useTaskEdit();

  async function handleAssigneeChange(command: Client.AssignTask) {
    setState((current) => current.withTask({ ...state.task.entry, assigneeIds: command.assigneeIds }));
  }

  return (<Box display='flex' alignItems='center'>
    <Typography><FormattedMessage id='core.taskCreate.fields.assignees' /></Typography>
    <TaskAssignees task={state.task} onChange={handleAssigneeChange} />
  </Box>
  )
}

const Roles: React.FC<{}> = () => {
  const { state, setState } = Context.useTaskEdit();

  async function handleRolesChange(command: Client.AssignTaskRoles) {
    setState((current) => current.withTask({ ...state.task.entry, roles: command.roles }))
  }

  return (<Box display='flex' alignItems='center'>
    <Typography><FormattedMessage id='core.taskCreate.fields.roles' /></Typography>
    <TaskRoles task={state.task} onChange={handleRolesChange} />
  </Box>
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


const Fields = { Title, Description, Status, Assignees, Roles, Priority, StartDate, DueDate };
export default Fields;
