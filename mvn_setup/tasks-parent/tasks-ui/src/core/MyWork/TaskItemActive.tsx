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
import { StyledAppBar, StyledTaskListTab } from '../TaskList';


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

const StyledViewHistoryButton = styled(Button)(({ theme }) => ({
  width: '100%',
  justifySelf: 'center',
  color: theme.palette.mainContent.main,
  fontWeight: 'bold',
  backgroundColor: theme.palette.uiElements.main,
  '&:hover': {
    backgroundColor: darken(theme.palette.uiElements.main, 0.3),
  }
}));

const StyledStack: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const theme = useTheme();

  return (<Box sx={{
    height: '100%',
    position: 'fixed',
    boxShadow: 1,
    width: '23%',
    pt: theme.spacing(2),
    px: theme.spacing(2),
    backgroundColor: theme.palette.background.paper
  }}>
    <Stack direction='column' spacing={1}>
      {children}
    </Stack>
  </Box>);
}

const MyRecentActivity: React.FC = () => {
  const org = Client.useOrg();
  const myActivities = org.state.iam.activity;

  /*  TODO
  sort by most recent date first
  show only the most recent 10 activities
  */
  return (
    <StyledStack>
      <Typography fontWeight='bold' variant='h4'><FormattedMessage id='core.myWork.recentActivities' /></Typography>
      <Divider sx={{ my: 1 }} />

      {myActivities.map((activity) => (
        <Box key={activity.id}>
          <Typography sx={{ fontWeight: 'bolder' }}>{activity.eventDate}</Typography>
          <Typography><FormattedMessage id={`core.myWork.recentActivities.events.${activity.eventType}`} />{`: ${activity.subjectTitle}`}</Typography>
        </Box>
      ))}
      <StyledViewHistoryButton><FormattedMessage id='core.myWork.button.myActivityHistory' /></StyledViewHistoryButton>

    </StyledStack>
  )
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

  if (task.assigneeGroupType === 'assigneeOverdue') {
    return { alertSeverity: 'error', isDueDate: true, title: 'core.teamSpace.task.overdue.alert' };
  }
  if (task.assigneeGroupType === 'assigneeStartsToday') {
    return { alertSeverity: 'warning', isDueDate: true, title: 'core.teamSpace.task.dueSoon.alert' }
  }
  if (task.assigneeGroupType === 'assigneeCurrentlyWorking') {
    return { alertSeverity: 'info', isDueDate: true, title: 'core.teamSpace.task.currentlyWorking.alert' }
  }
  return { alertSeverity: 'success', isDueDate: true, title: 'core.teamSpace.task.available.alert' }
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
  </StyledStack>);
}




const DelegateTaskItemActive: React.FC<{ task: Client.TaskDescriptor | undefined }> = ({ task }) => {

  // return summaryTab === 'summary' ? 
  const [summaryTab, setSummaryTab] = React.useState<'TaskItemActive' | 'MyRecentActivity'>('TaskItemActive');

  function handleSummaryTab() {
    setSummaryTab(summaryTab === 'TaskItemActive' ? 'MyRecentActivity' : 'TaskItemActive');
  }

  return (<>
    <StyledAppBar color={undefined}>
      <StyledTaskListTab active={summaryTab === 'TaskItemActive'} color={'#03256c'}
        onClick={handleSummaryTab}><FormattedMessage id='core.myWork.tab.taskSummary' />
      </StyledTaskListTab>
      <StyledTaskListTab active={summaryTab === 'MyRecentActivity'} color={'#b7245c'}
        onClick={handleSummaryTab}><FormattedMessage id='core.myWork.tab.recentActivities' />
      </StyledTaskListTab>
    </StyledAppBar>
    {summaryTab === 'MyRecentActivity' ? <MyRecentActivity /> : <TaskItemActive task={task} />}
  </>)

}




export default DelegateTaskItemActive;