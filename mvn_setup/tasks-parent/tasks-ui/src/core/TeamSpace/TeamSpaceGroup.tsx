import React from 'react';
import { Box, Divider, Alert, AlertTitle, IconButton, useTheme, Button, Typography, Tooltip, Chip } from '@mui/material';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';

import { FormattedMessage } from 'react-intl';
import Client from '@taskclient';

const StyledTaskItem: React.FC<{ task: Client.TaskDescriptor, onTask: (task: Client.TaskDescriptor) => void }> = ({ task, onTask }) => {
  const theme = useTheme();
  const [active, setActive] = React.useState(false);
  const taskDueDate = task.dueDate ? task.dueDate.toLocaleDateString() : undefined;
  const isCompletedOrRejected: boolean = task.status === 'COMPLETED' || task.status === 'REJECTED';

  if (isCompletedOrRejected) {
    return <></>;
  }

  return (
    <Box sx={{ my: 1 }} display='flex' alignItems='center'
      height={theme.typography.body2.fontSize} maxHeight={theme.typography.body2.fontSize}
      onMouseOver={() => {
        setActive(true);
        onTask(task);
      }}
      onMouseLeave={() => setActive(false)} >

      <Box width='7%' display='flex'>
        {active && <Tooltip arrow placement='top' title={<FormattedMessage id='core.teamSpace.tooltip.startWork' />}>
          <IconButton sx={{ color: 'uiElements.main' }}><ArrowForwardIosIcon /></IconButton>
        </Tooltip>}
      </Box>
      <Box width='50%' maxWidth='50%'>
        <Typography fontWeight='bolder' noWrap>{task.title}</Typography>
      </Box>
      <Box width='25%' sx={{ textAlign: 'right' }}><Typography>{taskDueDate}</Typography></Box>
      <Box display='flex' justifyContent='right'>{active && <IconButton><MoreHorizIcon /></IconButton>}</Box>
    </Box>
  );
}



const Header: React.FC<{ group: Client.Group }> = ({ group }) => {

  let title;
  let titleColor;

  if (group.id === 'groupOverdue') {
    title = <FormattedMessage id='task.overdue' />;
    titleColor = Client.TeamGroupPallete.groupOverdue;
  } else if (group.id === 'groupDueSoon') {
    titleColor = Client.TeamGroupPallete.groupDueSoon;
    title = <FormattedMessage id='task.dueSoon' />
  } else if (group.id === 'groupAvailable') {
    titleColor = Client.TeamGroupPallete.groupAvailable;
    title = <FormattedMessage id='task.available' />

  } else {
    throw new Error("Unknown group: " + group.id);
  }

  return (
    <Box display='flex' justifyContent='space-between'>
      <Box width='50%'><Typography variant='h4' fontWeight='bold' sx={{ color: titleColor }}>{title}</Typography></Box>
      <Box marginRight={5}><Typography textAlign='right' fontWeight='bold'><FormattedMessage id='task.dueDate' /></Typography></Box>
    </Box>
  )
}

const StyledAlert: React.FC<{
  children?: React.ReactNode,
  title: string,
  task: Client.TaskDescriptor,
  alertSeverity: 'error' | 'success' | 'warning',
  isDueDate?: boolean
}> = ({ isDueDate, title, task, alertSeverity }) => {

  const { dueDate } = task;

  return (
    <Alert severity={alertSeverity}>
      <AlertTitle><FormattedMessage id={title} /></AlertTitle>
      {isDueDate ? <Typography variant='body2' fontWeight='bolder'>{dueDate?.toUTCString()}</Typography> : undefined}
    </Alert>
  )
}


const SummaryAlert: React.FC<{ task: Client.TaskDescriptor }> = ({ task }) => {

  if (task.teamGroupType === 'groupOverdue') {
    return <StyledAlert alertSeverity='error' task={task} isDueDate title='core.teamSpace.task.overdue.alert' />
  }
  if (task.teamGroupType === 'groupDueSoon') {
    return <StyledAlert alertSeverity='warning' task={task} isDueDate title='core.teamSpace.task.dueSoon.alert' />
  }
  return (<StyledAlert alertSeverity='success' task={task} title='core.teamSpace.task.available.alert' />)
}


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

  const { assignees } = task;
  const sectionTitle = <Typography marginRight={1} fontWeight='bolder'><FormattedMessage id='task.assignees' /></Typography>

  if (!assignees.length) {
    return (<>
        {sectionTitle}
        <Typography><FormattedMessage id='task.assignees.none' /></Typography>
      </>
    )
  }

  return (<>
    {sectionTitle}
    {assignees.map((assignee) => <Typography>{assignee}</Typography>)}
  </>)
}


const SummaryTaskSelected: React.FC<{ task: Client.TaskDescriptor }> = ({ task }) => {
  const { title, description } = task;

  return (
    <>
      <Typography marginRight={1} fontWeight='bold' variant='h4'>{title}</Typography>
      <Divider sx={{ my: 1 }} />
      <Button variant='contained' color='info' endIcon={<ArrowForwardIosIcon />}><FormattedMessage id='task.start' /></Button>
      <Button variant='contained' color='warning'><FormattedMessage id='task.edit' /></Button>
      <Box sx={{ my: 1 }} />
      <SummaryAlert task={task} />
      <Typography marginRight={1} fontWeight='bolder'><FormattedMessage id='task.description' /></Typography>{description}
      <StyledAssignees task={task} />
      <StyledTaskStatus task={task} />
    </>
  )
}


const SummaryTaskNotSelected: React.FC = () => {
  return (<Alert severity='info'><AlertTitle><FormattedMessage id='core.teamSpace.summary.noneSelected' /></AlertTitle></Alert>)
}


export { Header, SummaryTaskSelected, SummaryTaskNotSelected, StyledTaskItem };