import React from 'react';
import { TextField, Typography, Stack, Box, IconButton, MenuList, MenuItem, ListItemText, Button, styled, SxProps, Checkbox, FormControl, FormControlLabel, Tooltip } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';
import DateRangeOutlinedIcon from '@mui/icons-material/DateRangeOutlined';
import MailOutlineIcon from '@mui/icons-material/MailOutline';
import AttachFileIcon from '@mui/icons-material/AttachFile';
import CircleNotificationsOutlinedIcon from '@mui/icons-material/CircleNotificationsOutlined';
import Burger from '@the-wrench-io/react-burger';
import TaskClient from '@taskclient';

import ChecklistDelegate from 'core/Checklist';
import { usePopover } from 'core/TaskTable/CellPopover';

const StyledMenuItem = styled(MenuItem)(({ theme }) => ({
  color: theme.palette.primary.contrastText,
  borderRadius: theme.shape.borderRadius,
  marginBottom: theme.spacing(1),
  ':hover': {
    color: theme.palette.text.secondary,
  }
}));

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
      return { backgroundColor: statusColors.COMPLETED, ':hover': { backgroundColor: statusColors.COMPLETED } };
    case 'CREATED':
      return { backgroundColor: statusColors.CREATED, ':hover': { backgroundColor: statusColors.CREATED } };
    case 'IN_PROGRESS':
      return { backgroundColor: statusColors.IN_PROGRESS, ':hover': { backgroundColor: statusColors.IN_PROGRESS } };
    case 'REJECTED':
      return { backgroundColor: statusColors.REJECTED, ':hover': { backgroundColor: statusColors.REJECTED } };
  }
}

const Status: React.FC<{}> = () => {
  const { state } = TaskClient.useTaskEdit();
  const status = state.task.status;
  const statusColorSx = getStatusColorConfig(status);
  const Popover = usePopover();
  const statusOptions: TaskClient.TaskStatus[] = ['CREATED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED'];

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start' }}>
      <Typography variant='h4'><FormattedMessage id='core.taskOps.editTask.status' /></Typography>
      <Box>
        <Button variant='contained' sx={{ mt: 1, ...statusColorSx }} onClick={Popover.onClick}><FormattedMessage id={'task.status.' + status} /></Button>
        <Tooltip title={<FormattedMessage id='core.taskOps.editTask.status.markAsCompleted' />}>
          <Checkbox
            checked={status === 'COMPLETED'}
            sx={{ mt: 1, color: TaskClient.StatusPallette.COMPLETED, '&.Mui-checked': { color: TaskClient.StatusPallette.COMPLETED } }}
          />
        </Tooltip>
      </Box>
      <Popover.Delegate>
        <MenuList dense>
          {statusOptions.map(option => <StyledMenuItem key={option} sx={getStatusColorConfig(option)}><ListItemText><FormattedMessage id={'task.status.' + option} /></ListItemText></StyledMenuItem>)}
        </MenuList>
      </Popover.Delegate>
    </Box>
  )
}

const Assignee: React.FC<{}> = () => {
  return (<Box>Assignee</Box>)
}

const Priority: React.FC<{}> = () => {
  return (<Box>Priority</Box>)
}

const Options: React.FC<{}> = () => {
  return (<Box>Options</Box>)
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

  return (
    <Box textAlign='center'>
      <Typography><FormattedMessage id='core.taskOps.editTask.startDate' /></Typography>
      <IconButton onClick={onClick} color='secondary'><DateRangeOutlinedIcon /></IconButton>
    </Box>);
}

const DueDate: React.FC<{ dueDate: string, onClick: () => void }> = ({ dueDate, onClick }) => {
  return (
    <Box textAlign='center' onClick={onClick}>
      <Typography><FormattedMessage id='core.taskOps.editTask.dueDate' /></Typography>
      <Typography variant='caption'>{dueDate}</Typography>
    </Box>)
}



const Fields = { Title, Description, Checklist, Status, Assignee, Priority, Options, StartDate, DueDate, MessageCount, AttachmentCount, NewItemNotification }
export default Fields;