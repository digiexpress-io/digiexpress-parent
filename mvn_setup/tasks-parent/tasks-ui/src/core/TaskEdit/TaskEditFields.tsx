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
  const { state } = Client.useTaskEdit();
  const intl = useIntl();

  return (<TextField
    placeholder={intl.formatMessage({ id: 'core.taskEdit.taskTitle' })}
    InputProps={{ sx: { fontSize: '20pt' } }}
    fullWidth
    value={state.task.title}
  />);
}

const Description: React.FC<{}> = () => {
  const { state } = Client.useTaskEdit();
  const intl = useIntl();

  return (<TextField placeholder={intl.formatMessage({ id: 'core.taskEdit.taskDescription' })} multiline rows={4} maxRows={6} fullWidth
    value={state.task.description} />);
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
  const { state } = Client.useTaskEdit();

  return (
    <TaskStatus task={state.task}/>
  )
}

const Assignee: React.FC<{}> = () => {
  const { state } = Client.useTaskEdit();

  return (
    <TaskAssignees task={state.task}/>
  )
}

const Priority: React.FC<{}> = () => {
  const { state } = Client.useTaskEdit();

  return (
    <TaskPriority task={state.task} priorityTextEnabled/>
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
