import React from 'react';
import { Alert, AlertTitle, Typography, Chip } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import Client from '@taskclient';


const statusColors = Client.StatusPallette;
const StyledTaskStatus: React.FC<{ task: Client.TaskDescriptor }> = ({ task }) => {

  const { status } = task;

  return (<>
    <Typography marginRight={1} fontWeight='bolder'><FormattedMessage id='task.status' /></Typography>
    <Chip sx={{
      width: 'fit-content',
      backgroundColor: statusColors[status],
      color: 'activeItem.light',
      fontWeight: 'bold'
    }}
      label={<FormattedMessage id={`task.status.${status}`} />} />
  </>
  )
}

const StyledAssignees: React.FC<{ task: Client.TaskDescriptor }> = ({ task }) => {

  const assignees = task.assignees.map(assignee => <Typography>{assignee}</Typography>);
  const noAssignees = <Typography><FormattedMessage id='task.assignees.none' /></Typography>

  return (
    <>
      <Typography marginRight={1} fontWeight='bolder'><FormattedMessage id='task.assignees' /></Typography>
      {task.assignees.length ? assignees : noAssignees}
    </>
  )
}


const StyledAlert: React.FC<{
  children?: React.ReactNode,
  title: string,
  task: Client.TaskDescriptor,
  alertSeverity: 'error' | 'success' | 'warning',
  isDueDate?: boolean
}> = ({ isDueDate, title, task, alertSeverity }) => {

  return (
    <Alert severity={alertSeverity} variant='standard'>
      <AlertTitle><FormattedMessage id={title} /></AlertTitle>
      {isDueDate ? <Typography variant='body2' fontWeight='bolder'>{task.dueDate?.toUTCString()}</Typography> : undefined}
    </Alert>
  )
}


const StyledSummaryAlert: React.FC<{ task: Client.TaskDescriptor }> = ({ task }) => {

  if (task.teamGroupType === 'groupOverdue') {
    return <StyledAlert alertSeverity='error' task={task} isDueDate title='core.teamSpace.task.overdue.alert' />
  }
  if (task.teamGroupType === 'groupDueSoon') {
    return <StyledAlert alertSeverity='warning' task={task} isDueDate title='core.teamSpace.task.dueSoon.alert' />
  }
  return (<StyledAlert alertSeverity='success' task={task} title='core.teamSpace.task.available.alert' />)
}


const StyledTaskDescription: React.FC<{ task: Client.TaskDescriptor }> = ({ task }) => {
  return (
    <>
      <Typography marginRight={1} fontWeight='bolder'><FormattedMessage id='task.description' /></Typography>
      <Typography>{task.description}</Typography>
    </>
  )
}


export { StyledAssignees, StyledTaskStatus, StyledSummaryAlert, StyledTaskDescription };












