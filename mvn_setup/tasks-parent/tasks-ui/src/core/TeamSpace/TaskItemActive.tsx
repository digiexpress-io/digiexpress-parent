import React from 'react';
import {
  Box, Stack, Alert, AlertTitle, Typography, Divider, Skeleton,
  darken, styled,
  Chip, Button, useTheme, AlertColor, Avatar
} from '@mui/material';
import { FormattedMessage } from 'react-intl';
import TaskWorkDialog from 'core/TaskWork';
import TaskEditDialog from 'core/TaskEdit';
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


const StyledStack: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const theme = useTheme();

  return (<Box sx={{
    height: '100%',
    position: 'fixed',
    width: '23%',
    boxShadow: 1,
    paddingTop: theme.spacing(2),
    paddingLeft: theme.spacing(2),
    paddingRight: theme.spacing(2),
    backgroundColor: theme.palette.background.paper
  }}>
    <Stack direction='column' spacing={1}>
      {children}
    </Stack>
  </Box>);
}

const StyledStatusChip: React.FC<{ children: Client.TaskStatus }> = ({ children }) => {
  const backgroundColors = Client.StatusPallette;
  return (<Chip label={<FormattedMessage id={`task.status.${children}`} />}
    sx={{
      width: 'fit-content',
      backgroundColor: backgroundColors[children],
      color: 'activeItem.light',
      fontWeight: 'bold'
    }} />);
}

const NoAssignee: React.FC = () => {
  return (
    <Box display='flex' alignItems='center'>
      <Avatar sx={{
        width: 24,
        height: 24,
        fontSize: 10,
        mr: 1,
        my: '5px'
      }} />
      <Typography><FormattedMessage id='task.assignees.none' /></Typography>
    </Box>
  )
}

const StyledAssignee: React.FC<{ assigneeName: string, avatar: string }> = ({ assigneeName, avatar }) => {
  const { state } = Client.useTasks();
  const assigneeColor = state.pallette.owners[assigneeName];

  return (
    <Box display='flex' alignItems='center'>
      <Avatar sx={{
        backgroundColor: assigneeColor,
        width: 24,
        height: 24,
        fontSize: 10,
        mr: 1,
        my: '5px'
      }}>{avatar}</Avatar>
      <Typography>{assigneeName}</Typography>
    </Box>)
}


const StyledTitle: React.FC<{ children: string }> = ({ children }) => {
  return (<Typography fontWeight='bold'><FormattedMessage id={children} /></Typography>)
}

function getTaskAlert(task: Client.TaskDescriptor): { isDueDate: boolean, title: string, alertSeverity: AlertColor } {

  if (task.teamGroupType === 'groupOverdue') {
    return { alertSeverity: 'error', isDueDate: true, title: 'core.teamSpace.task.overdue.alert' };
  }
  if (task.teamGroupType === 'groupDueSoon') {
    return { alertSeverity: 'warning', isDueDate: true, title: 'core.teamSpace.task.dueSoon.alert' }
  }
  return { alertSeverity: 'success', isDueDate: false, title: 'core.teamSpace.task.available.alert' }
}

const TaskItemActive: React.FC<{ task: Client.TaskDescriptor | undefined }> = ({ task }) => {
  const [taskWorkOpen, setTaskWorkOpen] = React.useState(false);
  const [taskEditOpen, setTaskEditOpen] = React.useState(false);

  function handleTaskWork() {
    setTaskWorkOpen(prev => !prev);
  }

  function handleTaskEdit() {
    setTaskEditOpen(prev => !prev);
  }


  if (task) {
    const alert = getTaskAlert(task);

    return (<>
      <TaskWorkDialog open={taskWorkOpen} onClose={handleTaskWork} task={task} />
      <TaskEditDialog open={taskEditOpen} onClose={handleTaskEdit} task={task} />
      <StyledStack>
        {/* header section */}
        <Typography fontWeight='bold' variant='h4'>{task.title}</Typography>
        <Divider sx={{ my: 1 }} />

        {/* buttons section */}
        <StyledStartTaskButton onClick={handleTaskWork}><FormattedMessage id='task.start' /></StyledStartTaskButton>
        <StyledEditTaskButton onClick={handleTaskEdit}><FormattedMessage id='task.edit' /></StyledEditTaskButton>
        <Box sx={{ my: 1 }} />

        {/* duedate alert section */}
        <Alert severity={alert.alertSeverity} variant='standard'>
          <AlertTitle><FormattedMessage id={alert.title} /></AlertTitle>
          {alert.isDueDate ? <Typography variant='body2' fontWeight='bolder'>{task.dueDate?.toUTCString()}</Typography> : undefined}
        </Alert>

        {/* description section */}
        <StyledTitle children='task.description' />
        <Typography>{task.description}</Typography>

        {/* assignee section */}

        <StyledTitle children='task.assignees' />
        <Stack>
          {task.assigneesAvatars.length ?
            (task.assigneesAvatars.map((assignee, index) => (
              <StyledAssignee key={index} assigneeName={assignee.value} avatar={assignee.twoletters} />))) : <NoAssignee />
          }
        </Stack>

        {/* status section */}
        <StyledTitle children='task.status' />
        <StyledStatusChip>{task.status}</StyledStatusChip>
      </StyledStack>
    </>

    );
  }

  return (<StyledStack>
    <Stack spacing={1}>
      <Skeleton animation={false} variant="rounded" width='100%' height={40} />
      <Skeleton animation={false} variant="rounded" width='100%' height={40} />
      <Skeleton animation={false} variant="rounded" width='100%' height={40} />

      <Skeleton animation={false} variant="text" width='100%' height='2rem' />
      <Skeleton animation={false} variant="rounded" width='100%' height={70} />

      <Skeleton animation={false} variant="text" width='100%' height='2rem' />
      <Skeleton animation={false} variant="text" width='85%' height='1rem' />
      <Skeleton animation={false} variant="text" width='35%' height='1rem' />
      <Skeleton animation={false} variant="text" width='60%' height='1rem' />

      <Skeleton animation={false} variant="text" width='100%' height='2rem' />
      <Skeleton animation={false} variant="rounded" width='25%' height={30} sx={{ borderRadius: '15px' }} />
    </Stack>
  </StyledStack>);
}




export default TaskItemActive;