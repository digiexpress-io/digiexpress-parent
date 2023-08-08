import React from 'react';
import { TextField, Typography, Stack, Box, IconButton } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';
import DateRangeOutlinedIcon from '@mui/icons-material/DateRangeOutlined';
import MailOutlineIcon from '@mui/icons-material/MailOutline';
import AttachFileIcon from '@mui/icons-material/AttachFile';
import CircleNotificationsOutlinedIcon from '@mui/icons-material/CircleNotificationsOutlined';
import Burger from '@the-wrench-io/react-burger';
import TaskClient from '@taskclient';



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

  return (<Typography sx={{ fontSize: '20pt' }}>{state.task.title}</Typography>);
}

const Description: React.FC<{}> = () => {
  const { state } = TaskClient.useTaskEdit();
  const intl = useIntl();

  return (<Typography variant='body1'>{state.task.description}</Typography>);
}

const Checklist: React.FC<{}> = () => {

  return (
    <Section title='core.taskOps.editTask.checklists' actions={<SectionAddButton />}>
      checklist content
    </Section>
  )
}

const Status: React.FC<{}> = () => {
  return (<Box>Status</Box>)
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