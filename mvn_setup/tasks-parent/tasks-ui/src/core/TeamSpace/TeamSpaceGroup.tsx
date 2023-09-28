import React from 'react';
import { Box, Divider, Alert, AlertTitle, IconButton, useTheme, Button, Typography, Tooltip, darken, styled } from '@mui/material';

import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';
import { FormattedMessage } from 'react-intl';
import { StyledAssignees, StyledTaskStatus, StyledSummaryAlert, StyledTaskDescription } from './SummaryStyles';
import Client from '@taskclient';



const StyledStartTaskButton = styled(Button)(({ theme }) => ({
  color: theme.palette.mainContent.main,
  fontWeight: 'bold',
  backgroundColor: theme.palette.uiElements.main,
  '&:hover': {
    backgroundColor: darken(theme.palette.uiElements.main, 0.3),
  }
}));


const StyledEditTaskButton = styled(Button)(({ theme }) => ({
  border: '1px solid',
  color: theme.palette.uiElements.main,
  fontWeight: 'bold',
  borderColor: theme.palette.uiElements.main,
  '&:hover': {
    borderColor: darken(theme.palette.uiElements.main, 0.3),
    color: darken(theme.palette.uiElements.main, 0.3)
  }
}));

const TaskItem: React.FC<{ task: Client.TaskDescriptor, onTask: (task: Client.TaskDescriptor) => void }> = ({ task, onTask }) => {
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

const SummaryTaskSelected: React.FC<{ task: Client.TaskDescriptor }> = ({ task }) => {

  return (
    <>
      <Typography marginRight={1} fontWeight='bold' variant='h4'>{task.title}</Typography>
      <Divider sx={{ my: 1 }} />
      <StyledStartTaskButton><FormattedMessage id='task.start' /></StyledStartTaskButton>
      <StyledEditTaskButton><FormattedMessage id='task.edit' /></StyledEditTaskButton>
      <Box sx={{ my: 1 }} />
      <StyledSummaryAlert task={task} />
      <StyledTaskDescription task={task} />
      <StyledAssignees task={task} />
      <StyledTaskStatus task={task} />
    </>
  )
}


const SummaryTaskNotSelected: React.FC = () => {
  return (<Alert severity='info'><AlertTitle><FormattedMessage id='core.teamSpace.summary.noneSelected' /></AlertTitle></Alert>)
}


export { Header, SummaryTaskSelected, SummaryTaskNotSelected, TaskItem };

