import React from 'react';
import { Box, Stack, Alert, AlertTitle, Typography, IconButton, Divider, Skeleton, darken, styled, Button, useTheme, AlertColor } from '@mui/material';
import EditIcon from '@mui/icons-material/ModeEditOutlineOutlined';
import CrmIcon from '@mui/icons-material/AdminPanelSettingsOutlined';
import { FormattedMessage } from 'react-intl';

import TimestampFormatter from 'timestamp';
import TaskAssignees from '../TaskAssignees';
import TaskRoles from '../TaskRoles';
import TaskStatus from '../TaskStatus';
import CRMDialog from '../CRM';
import TaskEditDialog from '../TaskEdit';

import Context from 'context';
import Client from 'client';
import { TaskDescriptor } from 'descriptor-task';
import Burger from 'components-burger';

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
    //height: 'vh',
    overflowY: 'scroll',
    overflowX: 'hidden',
    boxShadow: 1,
    width: '23%',
    pt: theme.spacing(2),
    px: theme.spacing(2),
    backgroundColor: theme.palette.background.paper
  }}>
    <Stack direction='column' spacing={1}>
      {children}
    </Stack>
  </Box >);
}

const MyRecentActivity: React.FC = () => {
  const org = Context.useOrg();
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


const StyledTitle: React.FC<{ children: string }> = ({ children }) => {
  return (<Typography fontWeight='bold'><FormattedMessage id={children} /></Typography>)
}

function getTaskAlert(task: TaskDescriptor): { isDueDate: boolean, title: string, alertSeverity: AlertColor, alertMsg: string } {

  if (task.assigneeGroupType === 'assigneeOverdue') {
    return { alertSeverity: 'error', isDueDate: true, title: 'core.teamSpace.task.overdue.alert', alertMsg: 'core.myWork.task.dueDate' }
  }
  if (task.assigneeGroupType === 'assigneeStartsToday') {
    return { alertSeverity: 'warning', isDueDate: true, title: 'core.teamSpace.task.dueSoon.alert', alertMsg: 'core.myWork.task.dueDate' }
  }
  if (task.assigneeGroupType === 'assigneeCurrentlyWorking') {
    return { alertSeverity: 'info', isDueDate: true, title: 'core.teamSpace.task.currentlyWorking.alert', alertMsg: 'core.myWork.task.dueDate' }
  }
  return { alertSeverity: 'success', isDueDate: true, title: 'core.teamSpace.task.available.alert', alertMsg: 'core.myWork.task.dueDate' }
}

const TaskItemActive: React.FC<{ task: TaskDescriptor | undefined }> = ({ task }) => {
  const [crmOpen, setCrmOpen] = React.useState(false);
  const [taskEditOpen, setTaskEditOpen] = React.useState(false);

  const tasks = Context.useTasks();
  const backend = Context.useBackend();

  async function handleStatusChange(command: Client.ChangeTaskStatus) {
    if (!task) {
      return;
    }
    await backend.task.updateActiveTask(task.id, [command]);
    await tasks.reload();
  }

  async function handleAssigneeChange(assigneeIds: Client.UserId[]) {
    if (!task) {
      return;
    }
    const command: Client.AssignTask = { assigneeIds, commandType: 'AssignTask', taskId: task.id };
    await backend.task.updateActiveTask(task.id, [command]);
    await tasks.reload();
  }

  async function handleRoleChange(command: Client.AssignTaskRoles) {
    if (!task) {
      return;
    }
    await backend.task.updateActiveTask(task.id, [command]);
    await tasks.reload();
  }

  function handleCrm() {
    setCrmOpen(prev => !prev);
  }

  function handleTaskEdit() {
    setTaskEditOpen(prev => !prev);
  }


  if (task) {
    const alert = getTaskAlert(task);

    return (<>
      <CRMDialog open={crmOpen} onClose={handleCrm} task={task} />
      <TaskEditDialog open={taskEditOpen} onClose={handleTaskEdit} task={task} />

      <StyledStack>

        {/* duedate alert section */}
        <Alert severity={alert.alertSeverity} variant='standard'>
          <AlertTitle><FormattedMessage id={alert.title} /></AlertTitle>
          <Typography variant='body2' fontWeight='bolder'>
            {task.dueDate ? <FormattedMessage id={alert.alertMsg} values={{ dueDate: <TimestampFormatter type='date' value={task.dueDate} /> }} />
              :
              <FormattedMessage id='task.dueDate.none' />}
          </Typography>
        </Alert>

        {/* buttons section */}
        <Burger.Section>
          <StyledTitle children='task.tools' />
          <Stack direction='row' spacing={1} justifyContent='center'>
            <IconButton onClick={handleTaskEdit}><EditIcon sx={{ color: 'uiElements.main' }} /></IconButton>
            <IconButton onClick={handleCrm}><CrmIcon sx={{ color: 'locale.dark' }} /></IconButton>
          </Stack>
        </Burger.Section>

        {/* title section */}
        <Burger.Section>
          <StyledTitle children='task.title' />
          <Typography fontWeight='bold'>{task.title}</Typography>
        </Burger.Section>

        {/* description section */}
        <Burger.Section>
          <StyledTitle children='task.description' />
          <Typography>{task.description}</Typography>
        </Burger.Section>

        {/* assignee section */}
        <Burger.Section>
          <StyledTitle children='task.assignees' />
          <TaskAssignees onChange={handleAssigneeChange} task={task} fullnames />
        </Burger.Section>

        {/* roles section */}
        <Burger.Section>
          <StyledTitle children='task.roles' />
          <Box sx={{ cursor: 'pointer' }}><TaskRoles onChange={handleRoleChange} task={task} fullnames /></Box>
        </Burger.Section>

        {/* status section */}
        <Burger.Section>
          <StyledTitle children='task.status' />
          <TaskStatus onChange={handleStatusChange} task={task} />
        </Burger.Section>

      </StyledStack >
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

const TaskItemActiveWithRefresh: React.FC<{ task: TaskDescriptor | undefined }> = ({ task }) => {
  const [dismount, setDismount] = React.useState(false);

  React.useEffect(() => {
    if (dismount) {
      setDismount(false);
    }
  }, [dismount]);

  React.useEffect(() => {
    setDismount(true);
  }, [task]);

  if (dismount) {
    return null;
  }

  return (<TaskItemActive task={task} />)
}


export default TaskItemActiveWithRefresh;