import React from 'react';
import { Box, Stack, Alert, AlertTitle, Typography, Skeleton, useTheme, AlertColor, IconButton } from '@mui/material';
import EditIcon from '@mui/icons-material/ModeEditOutlineOutlined';
import CrmIcon from '@mui/icons-material/AdminPanelSettingsOutlined';

import { FormattedMessage } from 'react-intl';

import TimestampFormatter from 'timestamp';
import TaskAssignees from 'taskcomponents/TaskAssignees';
import TaskRoles from 'taskcomponents/TaskRoles';
import TaskStatus from 'taskcomponents/TaskStatus';
import CRMDialog from 'taskcomponents/CRM';
import TaskEditDialog from 'taskcomponents/TaskEdit';
import Client from 'client';
import Context from 'context';
import { TaskDescriptor } from 'taskdescriptor';
import Section from 'section';



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
    const command = { assigneeIds, commandType: 'AssignTask', taskId: task.id };
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
    setCrmkOpen(prev => !prev);
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
        <Section>
          <StyledTitle children='task.tools' />
          <Stack direction='row' spacing={1} justifyContent='center'>
            <IconButton onClick={handleTaskEdit}><EditIcon sx={{ color: 'uiElements.main' }} /></IconButton>
            <IconButton onClick={handleCrm}><CrmIcon sx={{ color: 'locale.dark' }} /></IconButton>
          </Stack>
        </Section>

        {/* title section */}
        <Section>
          <StyledTitle children='task.title' />
          <Typography fontWeight='bold'>{task.title}</Typography>
        </Section>

        {/* description section */}
        <Section>
          <StyledTitle children='task.description' />
          <Typography>{task.description}</Typography>
        </Section>

        {/* assignee section */}
        <Section>
          <StyledTitle children='task.assignees' />
          <TaskAssignees onChange={handleAssigneeChange} task={task} fullnames />
        </Section>

        {/* roles section */}
        <Section>
          <StyledTitle children='task.roles' />
          <TaskRoles onChange={handleRoleChange} task={task} fullnames />
        </Section>

        {/* status section */}
        <Section>
          <StyledTitle children='task.status' />
          <TaskStatus onChange={handleStatusChange} task={task} />
        </Section>
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