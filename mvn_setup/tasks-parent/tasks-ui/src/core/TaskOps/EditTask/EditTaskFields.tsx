import React from 'react';
import { FormattedMessage, useIntl } from 'react-intl';

import {
  TextField, Typography, Stack, Box, MenuList,
  MenuItem, Button, SxProps, List, ListItem, ListItemText, Avatar,
  AvatarGroup, Checkbox, InputAdornment
} from '@mui/material';
import DateRangeOutlinedIcon from '@mui/icons-material/DateRangeOutlined';
import MailOutlineIcon from '@mui/icons-material/MailOutline';
import AttachFileIcon from '@mui/icons-material/AttachFile';
import CircleNotificationsOutlinedIcon from '@mui/icons-material/CircleNotificationsOutlined';
import CircleIcon from '@mui/icons-material/Circle';
import EmojiFlagsIcon from '@mui/icons-material/EmojiFlags';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import SearchIcon from '@mui/icons-material/Search';

import Burger from '@the-wrench-io/react-burger';
import TaskClient from '@taskclient';

import ChecklistDelegate from 'core/Checklist';
import { usePopover } from 'core/TaskTable/CellPopover';
import { AvatarCode } from 'taskclient/tasks-ctx-types';


const SectionAddButton: React.FC<{}> = () => {
  return (<Burger.PrimaryButton label={'buttons.add'} onClick={() => { }} />)
}

const Section: React.FC<{ children: React.ReactNode, title: string, actions: React.ReactNode }> = ({ children, title, actions }) => {
  return (
    <>
      <Stack direction='row' spacing={1} alignItems='center'>
        <Box sx={{ minWidth: "50%" }}>
          <Typography><FormattedMessage id={title} /></Typography>
        </Box>
        {actions}
      </Stack>
      {children}
    </>);
}


const Title: React.FC<{}> = () => {
  const { state } = TaskClient.useTaskEdit();
  const intl = useIntl();

  return (<TextField
    placeholder={intl.formatMessage({ id: 'core.taskOps.editTask.taskTitle' })}
    InputProps={{ sx: { fontSize: '20pt' } }}
    fullWidth
    value={state.task.title}
  />);
}

const Description: React.FC<{}> = () => {
  const { state } = TaskClient.useTaskEdit();
  const intl = useIntl();

  return (<TextField placeholder={intl.formatMessage({ id: 'core.taskOps.editTask.taskDescription' })} multiline rows={4} maxRows={6} fullWidth
    value={state.task.description} />);
}

const Checklist: React.FC<{}> = () => {
  const { state } = TaskClient.useTaskEdit();

  console.log(state);

  return (
    <>
      {state.task.checklist.map(item => (<ChecklistDelegate key={item.id} value={item} />))}
    </>
  )
}

const getStatusColorConfig = (status: TaskClient.TaskStatus): SxProps => {
  const statusColors = TaskClient.StatusPallette;
  switch (status) {
    case 'COMPLETED':
      return { color: statusColors.COMPLETED, ':hover': { color: statusColors.COMPLETED } };
    case 'CREATED':
      return { color: statusColors.CREATED, ':hover': { color: statusColors.CREATED } };
    case 'IN_PROGRESS':
      return { color: statusColors.IN_PROGRESS, ':hover': { color: statusColors.IN_PROGRESS } };
    case 'REJECTED':
      return { color: statusColors.REJECTED, ':hover': { color: statusColors.REJECTED } };
  }
}

const getPriorityColorConfig = (priority: TaskClient.TaskPriority): SxProps => {
  const priorityColors = TaskClient.PriorityPalette;
  switch (priority) {
    case 'LOW':
      return { color: priorityColors.LOW, ':hover': { color: priorityColors.LOW } };
    case 'MEDIUM':
      return { color: priorityColors.MEDIUM, ':hover': { color: priorityColors.MEDIUM } };
    case 'HIGH':
      return { color: priorityColors.HIGH, ':hover': { color: priorityColors.HIGH } };
  }
}

const Status: React.FC<{}> = () => {
  const { state } = TaskClient.useTaskEdit();
  const status = state.task.status;
  const Popover = usePopover();
  const statusOptions: TaskClient.TaskStatus[] = ['CREATED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED'];

  return (
    <Box>
      <Button variant='text' color='inherit' onClick={Popover.onClick} sx={{ textTransform: 'none' }}>
        <CircleIcon sx={{ mr: 1, ...getStatusColorConfig(status) }} />
        <Typography><FormattedMessage id={'task.status.' + status} /></Typography>
      </Button>
      <Popover.Delegate>
        <MenuList dense>
          {statusOptions.map(option => (
            <MenuItem key={option} onClick={Popover.onClose}>
              <CircleIcon sx={{ alignItems: 'center', mr: 1, ...getStatusColorConfig(option) }} />
              <ListItemText><FormattedMessage id={'task.status.' + option} /></ListItemText>
            </MenuItem>
          ))}
        </MenuList>
      </Popover.Delegate>
    </Box>
  )
}

const Assignee: React.FC<{}> = () => {
  const tasksContext = TaskClient.useTasks();
  const { state } = TaskClient.useTaskEdit();
  const assigneesAvatars = state.task.assigneesAvatars;
  const Popover = usePopover();
  const [searchString, setSearchString] = React.useState<string>('');

  const avatars = assigneesAvatars.map((entry, index) => {
    return (<Avatar key={index}
      sx={{
        bgcolor: tasksContext.state.pallette.owners[entry.value],
        width: 24,
        height: 24,
        fontSize: 10,
      }}>{entry.twoletters}</Avatar>
    );
  });
  avatars.push(<Avatar key='add-icon' sx={{ width: 24, height: 24, fontSize: 10 }}><PersonAddIcon sx={{ fontSize: 15 }} /></Avatar>)
  const avatarGroup = (avatars.length && <AvatarGroup spacing='medium' onClick={Popover.onClick}>{avatars}</AvatarGroup>);

  const demoUsers = [
    ['SV', 'sam vimes'],
    ['CI', 'carrot ironfoundersson'],
    ['CL', 'cherry littlefoot'],
    ['LV', 'lord vetinari'],
    ['NN', 'nobby nobbs'],
  ];
  const userAvatarCodes: AvatarCode[] = demoUsers.map(entry => { return { value: entry[1], twoletters: entry[0] } });
  const filteredUserAvatarCodes = searchString !== '' ? userAvatarCodes.filter(entry => entry.value.toLowerCase().includes(searchString.toLowerCase())) : userAvatarCodes;
  const userAvatars = filteredUserAvatarCodes.map((entry, index) => {
    return (
      <>
        <ListItem key={index}>
          <Checkbox checked={assigneesAvatars.find(a => a.value === entry.value) !== undefined} />
          <Avatar key={index}
            sx={{
              bgcolor: tasksContext.state.pallette.owners[entry.value],
              width: 24,
              height: 24,
              fontSize: 10,
              mr: 1,
            }}>{entry.twoletters}</Avatar>
          <ListItemText>{entry.value}</ListItemText>
        </ListItem>
      </>
    );
  });

  return (
    <Box>
      <Button variant='text' color='inherit'>
        {avatarGroup}
      </Button>
      <Popover.Delegate>
        <TextField
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon color='primary' />
              </InputAdornment>
            ),
          }}
          fullWidth
          variant='standard'
          placeholder='Search'
          value={searchString}
          onChange={(e) => setSearchString(e.target.value)}
        />
        <List dense>
          {userAvatars}
        </List>
      </Popover.Delegate>
    </Box>
  )
}

const Priority: React.FC<{}> = () => {
  const { state } = TaskClient.useTaskEdit();
  const priority = state.task.priority;
  const Popover = usePopover();
  const priorityOptions: TaskClient.TaskPriority[] = ['LOW', 'HIGH', 'MEDIUM'];

  return (
    <Box>
      <Button variant='text' color='inherit' onClick={Popover.onClick} sx={{ textTransform: 'none' }}>
        <EmojiFlagsIcon sx={{ mr: 1, ...getPriorityColorConfig(priority) }} />
        <Typography><FormattedMessage id={'task.priority.' + priority} /></Typography>
      </Button>
      <Popover.Delegate>
        <MenuList dense>
          {priorityOptions.map(option => (
            <MenuItem key={option} onClick={Popover.onClose}>
              <EmojiFlagsIcon sx={{ alignItems: 'center', mr: 1, ...getPriorityColorConfig(option) }} />
              <ListItemText><FormattedMessage id={'task.priority.' + option} /></ListItemText>
            </MenuItem>
          ))}
        </MenuList>
      </Popover.Delegate>
    </Box>
  )
}

const Options: React.FC<{}> = () => {
  return (<Box><Button variant='text' color='inherit' sx={{ textTransform: 'none' }}><Typography>Options</Typography></Button></Box>)
}

const MessageCount: React.FC<{}> = () => {
  return (<Box display='flex' alignItems='center'><MailOutlineIcon />4</Box>)
}

const AttachmentCount: React.FC<{}> = () => {
  return (<Box display='flex' alignItems='center'><AttachFileIcon />2</Box>)
}

const NewItemNotification: React.FC<{}> = () => {
  return (<Box display='flex' alignItems='center'><CircleNotificationsOutlinedIcon />3</Box>)
}

const StartDate: React.FC<{ onClick: () => void }> = ({ onClick }) => {
  const { state } = TaskClient.useTaskEdit();
  const startDate = state.task.startDate;

  return (
    <Stack spacing={1} direction='row' alignItems='center'>
      <Typography variant='body2'><FormattedMessage id='core.taskOps.workOnTask.startDate' /></Typography>
      <Button onClick={onClick}>{startDate ? <Typography>{startDate.toLocaleDateString()}</Typography> : <DateRangeOutlinedIcon />}</Button>
    </Stack>
  );
}

const DueDate: React.FC<{ onClick: () => void }> = ({ onClick }) => {
  const { state } = TaskClient.useTaskEdit();
  const dueDate = state.task.dueDate;

  return (
    <Stack spacing={1} direction='row' alignItems='center'>
      <Typography variant='body2'><FormattedMessage id='core.taskOps.workOnTask.dueDate' /></Typography>
      <Button onClick={onClick}>{dueDate ? <Typography>{dueDate.toLocaleDateString()}</Typography> : <DateRangeOutlinedIcon />}</Button>
    </Stack>
  );
}


const Fields = { Title, Description, Checklist, Status, Assignee, Priority, Options, StartDate, DueDate, MessageCount, AttachmentCount, NewItemNotification }
export default Fields;
