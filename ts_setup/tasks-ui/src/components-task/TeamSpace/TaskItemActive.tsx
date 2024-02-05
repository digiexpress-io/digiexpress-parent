import React from 'react';
import { Box, Stack, Alert, AlertTitle, Typography, Skeleton, useTheme, AlertColor, IconButton } from '@mui/material';
import EditIcon from '@mui/icons-material/ModeEditOutlineOutlined';
import CrmIcon from '@mui/icons-material/AdminPanelSettingsOutlined';

import { FormattedMessage } from 'react-intl';

import Customer from 'components-customer';
import Burger from 'components-burger';


import Context from 'context';
import { TaskDescriptor, ChangeTaskStatus, AssignTaskRoles } from 'descriptor-task';
import { cyan } from 'components-colors';


import TaskAssignees from '../TaskAssignees';
import TaskRoles from '../TaskRoles';
import TaskStatus from '../TaskStatus';
import TaskEditDialog from '../TaskEdit';



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


const StyledTitle: React.FC<{ children: string }> = ({ children }) => {
  return (<Typography fontWeight='bold'><FormattedMessage id={children} /></Typography>)
}

function getTaskAlert(task: TaskDescriptor): { title: string, alertSeverity: AlertColor, alertMsg: string } {

  if (task.teamGroupType === 'groupOverdue') {
    return { alertSeverity: 'error', title: 'core.teamSpace.task.overdue.alert', alertMsg: 'core.teamSpace.task.dueDate' };
  }
  if (task.teamGroupType === 'groupDueSoon') {
    return { alertSeverity: 'warning', title: 'core.teamSpace.task.dueSoon.alert', alertMsg: 'core.teamSpace.task.dueDate' }
  }
  return { alertSeverity: 'success', title: 'core.teamSpace.task.available.alert', alertMsg: 'core.teamSpace.task.dueDate' }
}

const TaskItemActive: React.FC<{ task: TaskDescriptor | undefined }> = ({ task }) => {
  const [crmOpen, setCrmkOpen] = React.useState(false);
  const [taskEditOpen, setTaskEditOpen] = React.useState(false);
  const tasks = Context.useTasks();

  async function handleStatusChange(command: ChangeTaskStatus) {
    if (!task) {
      return;
    }
    await tasks.updateActiveTask(task.id, [command]);
  }

  async function handleAssigneeChange(assigneeIds: string[]) {
    if (!task) {
      return;
    }
    const command = { assigneeIds, commandType: 'AssignTask', taskId: task.id };
    await tasks.updateActiveTask(task.id, [command]);
  }

  async function handleRoleChange(command: AssignTaskRoles) {
    if (!task) {
      return;
    }
    await tasks.updateActiveTask(task.id, [command]);
    
  }

  function handleCrm() {
    setCrmkOpen(prev => !prev);
  }

  function handleTaskEdit() {
    setTaskEditOpen(prev => !prev);
  }


  if (task) {
    const alert = getTaskAlert(task);

    return (<>
      <Customer.CustomerDetailsDialog open={crmOpen} onClose={handleCrm} task={task} />
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
              <IconButton onClick={handleTaskEdit}><EditIcon sx={{ color:cyan }} /></IconButton>
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
          <TaskRoles onChange={handleRoleChange} task={task} fullnames />
        </Burger.Section>

        {/* status section */}
        <Burger.Section>
          <StyledTitle children='task.status' />
          <TaskStatus onChange={handleStatusChange} task={task} />
        </Burger.Section>
      </StyledStack>
    </>

    );
  }

  return (<StyledStack>
    <Stack spacing={1}>
      <Skeleton animation={false} variant="circular" width='100%' height={40} />
      <Skeleton animation={false} variant="circular" width='100%' height={40} />
      <Skeleton animation={false} variant="circular" width='100%' height={40} />

      <Skeleton animation={false} variant="text" width='100%' height='2rem' />
      <Skeleton animation={false} variant="circular" width='100%' height={70} />

      <Skeleton animation={false} variant="text" width='100%' height='2rem' />
      <Skeleton animation={false} variant="text" width='85%' height='1rem' />
      <Skeleton animation={false} variant="text" width='35%' height='1rem' />
      <Skeleton animation={false} variant="text" width='60%' height='1rem' />

      <Skeleton animation={false} variant="text" width='100%' height='2rem' />
      <Skeleton animation={false} variant="circular" width='25%' height={30} sx={{ borderRadius: '15px' }} />
    </Stack>
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