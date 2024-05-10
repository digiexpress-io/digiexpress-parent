import React from 'react';
import { Box, Stack, Alert, AlertTitle, Typography, IconButton, useTheme, AlertColor } from '@mui/material';
import EditIcon from '@mui/icons-material/ModeEditOutlineOutlined';
import CrmIcon from '@mui/icons-material/AdminPanelSettingsOutlined';
import { FormattedMessage } from 'react-intl';

import TaskAssignees from '../TaskAssignees';
import TaskRoles from '../TaskRoles';
import TaskStatus from '../TaskStatus';
import TaskEditDialog from '../TaskEdit';


import { TaskDescriptor, ChangeTaskStatus, AssignTaskRoles, AssignTask, useTasks } from 'descriptor-task';

import Burger from 'components-burger';
import { cyan } from 'components-colors';
import { PrincipalId } from 'descriptor-access-mgmt';
import { CustomerDetailsDialog } from 'components-customer';



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



const StyledTitle: React.FC<{ children: string }> = ({ children }) => {
  return (<Typography fontWeight='bold'><FormattedMessage id={children} /></Typography>)
}

function getTaskAlert(task: TaskDescriptor): { isDueDate: boolean, title: string, alertSeverity: AlertColor, alertMsg: string } {

  if (task.assigneeGroupType === 'assigneeOverdue') {
    return { alertSeverity: 'error', isDueDate: true, title: 'core.myWork.task.overdue.alert', alertMsg: 'core.myWork.task.dueDate' }
  }
  if (task.assigneeGroupType === 'assigneeStartsToday') {
    return { alertSeverity: 'warning', isDueDate: true, title: 'core.myWork.task.startsToday.alert', alertMsg: 'core.myWork.task.dueDate' }
  }
  if (task.assigneeGroupType === 'assigneeCurrentlyWorking') {
    return { alertSeverity: 'info', isDueDate: true, title: 'core.myWork.task.currentlyWorking.alert', alertMsg: 'core.myWork.task.dueDate' }
  }
  return { alertSeverity: 'success', isDueDate: true, title: 'core.myWork.task.available.alert', alertMsg: 'core.myWork.task.dueDate' }
}

const TaskItemActive: React.FC<{ task: TaskDescriptor | undefined }> = ({ task }) => {
  const [crmOpen, setCrmOpen] = React.useState(false);
  const [taskEditOpen, setTaskEditOpen] = React.useState(false);

  const tasks = useTasks();

  async function handleStatusChange(command: ChangeTaskStatus) {
    if (!task) {
      return;
    }
    await tasks.updateActiveTask(task.id, [command]);
  }

  async function handleAssigneeChange(assigneeIds: PrincipalId[]) {
    if (!task) {
      return;
    }
    const command: AssignTask = { assigneeIds, commandType: 'AssignTask', taskId: task.id };
    await tasks.updateActiveTask(task.id, [command]);
  }

  async function handleRoleChange(command: AssignTaskRoles) {
    if (!task) {
      return;
    }
    await tasks.updateActiveTask(task.id, [command]);
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
      <CustomerDetailsDialog open={crmOpen} onClose={handleCrm} customer={task.customerId} />
      <TaskEditDialog open={taskEditOpen} onClose={handleTaskEdit} task={task} />

      <StyledStack>

        {/* duedate alert section */}
        <Alert severity={alert.alertSeverity} variant='standard'>
          <AlertTitle><FormattedMessage id={alert.title} /></AlertTitle>
          <Typography variant='body2' fontWeight='bolder'>
            {task.dueDate ? <FormattedMessage id={alert.alertMsg} values={{ dueDate: <Burger.DateTimeFormatter type='date' value={task.dueDate} /> }} />
              :
              <FormattedMessage id='task.dueDate.none' />}
          </Typography>
        </Alert>

        {/* buttons section */}


        <Burger.Section>
          <StyledTitle children='task.tools' />
          <Stack direction='row' spacing={1} justifyContent='center'>
            <Box display='flex' flexDirection='column' alignItems='center'>
              <IconButton onClick={handleTaskEdit}><EditIcon sx={{ color: cyan }} /></IconButton>
              <Typography><FormattedMessage id='task.edit' /></Typography>
            </Box>
            <Box display='flex' flexDirection='column' alignItems='center'>
              <IconButton onClick={handleCrm}><CrmIcon sx={{ color: 'locale.dark' }} /></IconButton>
              <Typography><FormattedMessage id='customer.details.view' /></Typography>
            </Box>
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

  return (null);
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