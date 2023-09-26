import React from 'react';
import { Box, Divider, Alert, AlertTitle, IconButton, useTheme, Button, Typography, Tooltip } from '@mui/material';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';
import { FormattedMessage } from 'react-intl';
import Client from '@taskclient';

const StyledTaskItem: React.FC<{ task: Client.TaskDescriptor, onTask: (task: Client.TaskDescriptor) => void }> = ({ task, onTask }) => {
  const theme = useTheme();
  const [active, setActive] = React.useState(false);
  const taskDueDate = task.dueDate?.toLocaleDateString();

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
  const { assignees, dueDate, status, title, description } = task;

  return (<>
    <Typography marginRight={1} fontWeight='bold' variant='h4'>{title}</Typography>
    <Alert severity='error'><AlertTitle><FormattedMessage id='task.overdue.msg' /></AlertTitle>
      {dueDate?.toUTCString()}</Alert>
    <Divider sx={{ my: 1 }} />
    <Button variant='contained' color='info' endIcon={<ArrowForwardIosIcon />}><FormattedMessage id='task.start' /></Button>
    <Button variant='contained' color='warning'><FormattedMessage id='task.edit' /></Button>
    <Box sx={{ my: 1 }} />

    <Typography marginRight={1} fontWeight='bolder'><FormattedMessage id='task.description' /></Typography>{description}
    <Typography marginRight={1} fontWeight='bolder'><FormattedMessage id='task.assignees' /></Typography>{JSON.stringify(assignees)}
    <Typography marginRight={1} fontWeight='bolder'><FormattedMessage id='task.status' /></Typography>{status}
  </>
  )
}


const SummaryTaskNotSelected: React.FC<{}> = () => {
  return (<>empty</>)
}


export { Header, SummaryTaskSelected, SummaryTaskNotSelected, StyledTaskItem };