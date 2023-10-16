import React from 'react';
import { FormattedMessage, useIntl } from 'react-intl';

import { TextField, Typography, Stack, Box, IconButton, Button } from '@mui/material';
import DateRangeOutlinedIcon from '@mui/icons-material/DateRangeOutlined';
import MailOutlineIcon from '@mui/icons-material/MailOutline';
import AttachFileIcon from '@mui/icons-material/AttachFile';
import CircleNotificationsOutlinedIcon from '@mui/icons-material/CircleNotificationsOutlined';
import CloseIcon from '@mui/icons-material/Close';

import Client from '@taskclient';

import ChecklistDelegate from 'core/Checklist';
import TaskAssignees from 'core/TaskAssignees';
import TaskStatus from 'core/TaskStatus';
import TaskPriority from 'core/TaskPriority';


const Title: React.FC<{}> = () => {
  const { state, setState } = Client.useTaskEdit();
  const backend = Client.useBackend();

  const intl = useIntl();
  const [title, setTitle] = React.useState(state.task.title);

  function handleTitleChange(event: React.ChangeEvent<HTMLInputElement>) {
    setTitle(event.target.value);
  }

  async function handleChange() {
    const command: Client.ChangeTaskInfo = {
      commandType: 'ChangeTaskInfo',
      taskId: state.task.id,
      description: state.task.description,
      title
    };
    const updatedTask = await backend.task.updateActiveTask(state.task.id, [command]);
    setState((current) => current.withTask(updatedTask));
  }

  return (<TextField
    placeholder={intl.formatMessage({ id: 'core.taskEdit.taskTitle' })}
    InputProps={{ sx: { fontSize: '20pt' } }}
    fullWidth
    value={title}
    onChange={handleTitleChange}
    onBlur={handleChange}
  />);
}

const Description: React.FC<{}> = () => {
  const { state, setState } = Client.useTaskEdit();
  const [description, setDescription] = React.useState(state.task.description);
  const backend = Client.useBackend();
  const intl = useIntl();

  function handleDescriptionChange(event: React.ChangeEvent<HTMLInputElement>) {
    setDescription(event.target.value);
  }

  async function handleChange() {
    const command: Client.ChangeTaskInfo = {
      commandType: 'ChangeTaskInfo',
      taskId: state.task.id,
      title: state.task.title,
      description
    };
    const updatedTask = await backend.task.updateActiveTask(state.task.id, [command]);
    setState((current) => current.withTask(updatedTask));
  }

  return (<TextField
    placeholder={intl.formatMessage({ id: 'core.taskEdit.taskDescription' })}
    fullWidth multiline minRows={4} maxRows={6}
    value={description}
    onChange={handleDescriptionChange}
    onBlur={handleChange}
  />);
}

const Checklist: React.FC<{}> = () => {
  const { state } = Client.useTaskEdit();

  return (
    <>
      {state.task.checklist.map(item => (<ChecklistDelegate key={item.id} value={item} />))}
    </>
  )
}

const Status: React.FC<{}> = () => {
  const { state, setState } = Client.useTaskEdit();
  const backend = Client.useBackend();

  async function handleStatusChange(command: Client.ChangeTaskStatus) {
    const updatedTask = await backend.task.updateActiveTask(state.task.id, [command]);
    setState((current) => current.withTask(updatedTask));
  }

  return (
    <TaskStatus task={state.task} onChange={handleStatusChange} />
  )
}

const Assignee: React.FC<{}> = () => {
  const { state, setState } = Client.useTaskEdit();
  const backend = Client.useBackend();

  async function handleAssigneeChange(command: Client.AssignTask) {
    const updatedTask = await backend.task.updateActiveTask(state.task.id, [command]);
    setState((current) => current.withTask(updatedTask));
  }

  return (
    <TaskAssignees task={state.task} onChange={handleAssigneeChange} />
  )
}

const Priority: React.FC<{}> = () => {
  const { state, setState } = Client.useTaskEdit();
  const backend = Client.useBackend();

  async function handlePriorityChange(command: Client.ChangeTaskPriority) {
    const updatedTask = await backend.task.updateActiveTask(state.task.id, [command]);
    setState((current) => current.withTask(updatedTask));
  }

  return (
    <TaskPriority task={state.task} priorityTextEnabled onChange={handlePriorityChange} />
  )
}

const Options: React.FC<{}> = () => {
  return (<Box><Button variant='text' color='inherit' sx={{ textTransform: 'none' }}><Typography>Options</Typography></Button></Box>)
}

const MessageCount: React.FC<{}> = () => {
  return (<Box display='flex' alignItems='center'><MailOutlineIcon />4</Box>)
}

const AttachmentCount: React.FC<{}> = () => {
  return (<Box display='flex' alignItems='center'><AttachFileIcon />2</Box>)
}

const NewItemNotification: React.FC<{}> = () => {
  return (<Box display='flex' alignItems='center'><CircleNotificationsOutlinedIcon />3</Box>)
}

const StartDate: React.FC<{ onClick: () => void }> = ({ onClick }) => {
  const { state } = Client.useTaskEdit();
  const startDate = state.task.startDate;

  return (
    <Stack spacing={1} direction='row' alignItems='center'>
      <Typography variant='body2'><FormattedMessage id='core.taskWork.startDate' /></Typography>
      <Button onClick={onClick}>{startDate ? <Typography>{startDate.toLocaleDateString()}</Typography> : <DateRangeOutlinedIcon />}</Button>
    </Stack>
  );
}

const DueDate: React.FC<{ onClick: () => void }> = ({ onClick }) => {
  const { state } = Client.useTaskEdit();
  const dueDate = state.task.dueDate;

  return (
    <Stack spacing={1} direction='row' alignItems='center'>
      <Typography variant='body2'><FormattedMessage id='core.taskWork.dueDate' /></Typography>
      <Button onClick={onClick}>{dueDate ? <Typography>{dueDate.toLocaleDateString()}</Typography> : <DateRangeOutlinedIcon />}</Button>
    </Stack>
  );
}

const CloseDialogButton: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  return (
    <IconButton onClick={onClose}>
      <CloseIcon />
    </IconButton>
  )
}

const Fields = {
  Title, Description, Checklist, Status, Assignee, Priority,
  Options, StartDate, DueDate, MessageCount, AttachmentCount,
  NewItemNotification, CloseDialogButton
}
export default Fields;
