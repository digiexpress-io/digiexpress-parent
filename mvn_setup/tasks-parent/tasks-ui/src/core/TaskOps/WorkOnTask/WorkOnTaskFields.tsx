import React from 'react';
import { FormattedMessage } from 'react-intl';

import {
  Typography, Stack, Box, IconButton,
  Button, List, ListItem, styled, Alert, Avatar, Dialog,
  DialogTitle, DialogActions, DialogContent, TextareaAutosize, alpha, useTheme
} from '@mui/material';
import MailOutlineIcon from '@mui/icons-material/MailOutline';
import AttachFileIcon from '@mui/icons-material/AttachFile';
import AttachEmailIcon from '@mui/icons-material/AttachEmail';
import DownloadIcon from '@mui/icons-material/Download';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import ForumIcon from '@mui/icons-material/Forum';
import SupervisedUserCircleIcon from '@mui/icons-material/SupervisedUserCircle';
import ReplyIcon from '@mui/icons-material/Reply';
import ArchiveIcon from '@mui/icons-material/Archive';
import AssignmentTurnedInIcon from '@mui/icons-material/AssignmentTurnedIn';
import CloseIcon from '@mui/icons-material/Close';
import CallMadeIcon from '@mui/icons-material/CallMade';
import CallReceivedIcon from '@mui/icons-material/CallReceived';

import TaskClient from '@taskclient';

import ChecklistDelegate from 'core/Checklist';
import { TaskExtension } from 'taskclient/task-types';
import { Message, Thread } from 'core/Inbox/thread-types';
import { AttachmentAndDateTime } from 'core/Inbox/ThreadPreview';
import { demoThreads } from 'core/Inbox/DemoThreads';


const StyledListItem = styled(ListItem)(({ theme }) => ({
  borderTop: '1px solid',
  borderBottom: '1px solid',
  borderColor: theme.palette.divider,
}))

const StyledDialogActions = styled(DialogActions)(({ theme }) => ({
  display: 'flex',
  justifyContent: 'space-between',
  marginLeft: theme.spacing(2),
  marginRight: theme.spacing(1),
  marginBottom: theme.spacing(2),
}))

const StyledTextArea = styled(TextareaAutosize)(({ theme }) => ({
  fontFamily: theme.typography.fontFamily,
  fontSize: theme.typography.fontSize,
  fontWeight: theme.typography.fontWeightRegular,
  padding: theme.spacing(1),
  borderRadius: '12px 12px 0 12px',
  width: '30vw',
}))

const ListItemContainer = styled(Box)({
  display: 'flex',
  flexDirection: 'row',
  justifyContent: 'space-between',
  width: '100%',
})

const PaddedTypography = styled(Typography)(({ theme }) => ({
  padding: theme.spacing(1),
}))

const ExpandableMessageContainer = styled(Box)(({ theme }) => ({
  borderTop: '1px solid',
  borderBottom: '1px solid',
  borderColor: theme.palette.divider,
  ':hover': {
    cursor: 'pointer',
    borderColor: theme.palette.warning.main,
  }
}))

const MessageHeaderContainer = styled(Box)({
  display: 'flex',
  flexDirection: 'row',
  justifyContent: 'space-between',
  position: 'relative',
})

const ChecklistAlert = styled(Alert)(({ theme }) => ({
  backgroundColor: alpha(theme.palette.primary.light, 0.12),
  '& .MuiSvgIcon-root': {
    color: theme.palette.primary.main,
  }
}))


const Title: React.FC<{}> = () => {
  const { state } = TaskClient.useTaskEdit();

  return (<Typography variant='h4'>{state.task.title}</Typography>);
}

const Description: React.FC<{}> = () => {
  const { state } = TaskClient.useTaskEdit();

  return (<Typography variant='body1'>{state.task.description}</Typography>);
}

const Checklist: React.FC<{}> = () => {
  const { state } = TaskClient.useTaskEdit();
  const checklist = state.task.checklist;

  return (
    <>
      {checklist.length ?
        checklist.map(item => (<ChecklistDelegate key={item.id} value={item} />)) :
        <Box sx={{ my: 2, mx: 1 }}>
          <ChecklistAlert severity='info' ><FormattedMessage id='core.taskOps.workOnTask.checklists.none' /></ChecklistAlert>
          <Button variant='outlined' startIcon={<AddIcon />} sx={{ textTransform: 'none', mt: 1 }}>
            <Typography><FormattedMessage id='core.taskOps.workOnTask.checklists.add' /></Typography>
          </Button>
        </Box>
      }
    </>
  )
}

const StartDate: React.FC = () => {
  const { state } = TaskClient.useTaskEdit();
  const startDate = state.task.startDate;
  if (!startDate) {
    return <></>;
  }

  return (
    <Stack spacing={1} direction='row' alignItems='center'>
      <Typography variant='body2'><FormattedMessage id='core.taskOps.workOnTask.startDate' /></Typography>
      <Typography>{startDate.toLocaleDateString()}</Typography>
    </Stack>
  );
}

const DueDate: React.FC = () => {
  const { state } = TaskClient.useTaskEdit();
  const dueDate = state.task.dueDate;
  if (!dueDate) {
    return <></>;
  }

  return (
    <Stack spacing={1} direction='row' alignItems='center' >
      <Typography variant='body2'><FormattedMessage id='core.taskOps.workOnTask.dueDate' /></Typography>
      <Typography>{dueDate.toLocaleDateString()}</Typography>
    </Stack>
  );
}

const AttachmentListItem: React.FC<{ attachment: TaskExtension }> = ({ attachment }) => {
  const theme = useTheme();
  const backgroundColor = attachment.id.includes('2') ? theme.palette.background.paper : alpha(theme.palette.info.main, 0.1);
  const icon = attachment.id.includes('2') ? <CallMadeIcon color='info' /> : <CallReceivedIcon color='info' />;
  return (
    <StyledListItem sx={{ backgroundColor }}>
      <ListItemContainer>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          {icon}
          <PaddedTypography>{attachment.name}</PaddedTypography>
        </Box>
        <Box>
          <IconButton>
            <DownloadIcon color='info' />
          </IconButton>
          <IconButton>
            <DeleteIcon color='error' />
          </IconButton>
        </Box>
      </ListItemContainer>
    </StyledListItem>
  );
}

const Attachments: React.FC<{}> = () => {
  const { state } = TaskClient.useTaskEdit();
  const attachments: TaskExtension[] = state.task.uploads;
  return (
    <Box sx={{ p: 1 }}>
      {attachments.length ? <List>
        {attachments.map(attachment => <AttachmentListItem key={attachment.id} attachment={attachment} />)}
      </List> : <Alert severity='info' sx={{ my: 1 }}><FormattedMessage id='core.taskOps.workOnTask.attachments.none' /></Alert>}
      <Button variant='outlined' color='info' startIcon={<AddIcon />} sx={{ textTransform: 'none' }}>
        <Typography><FormattedMessage id='core.taskOps.workOnTask.attachments.add' /></Typography>
      </Button>
    </Box>
  );
}

const Form: React.FC<{}> = () => {
  const { state } = TaskClient.useTaskEdit();
  const form: TaskExtension = state.task.entry.extensions.filter(extension => extension.type === 'dialob')[0];
  return (
    <Box sx={{ border: 1, borderRadius: 1, py: 20, pl: 1, m: 1, mt: 2 }}>
      {form ? <>
        <Typography variant='h4' sx={{ mb: 2 }}>{form.name}</Typography>
        <Typography variant='body1'>{form.body}</Typography>
      </> : <Typography>Form should be here</Typography>}
    </Box>
  );
}

const UserAvatar: React.FC<{ userName: string, representerName?: string }> = ({ userName, representerName }) => {
  const avatarText = userName.split(' ').map(name => name[0]).join('').toUpperCase();
  const avatarIcon = <SupervisedUserCircleIcon fontSize='large' />;
  const avatarColor = userName === 'Office Worker' ? 'warning.light' : 'warning.dark';

  return (
    <Avatar sx={{ bgcolor: avatarColor, m: 1 }} >{representerName ? avatarIcon : avatarText}</Avatar>
  )
}

const MessageExpandedSection: React.FC<{ message: Message }> = ({ message }) => {
  const { attachments } = message;
  const hasAttachments = attachments.length > 0;

  const handleClick = (e: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
    e.stopPropagation();
  }


  return (
    <Box sx={{ display: 'flex', alignItems: 'center' }}>
      {hasAttachments && attachments.map(attachment =>
        <Button key={attachment.id} startIcon={<DownloadIcon />} variant='outlined' color='warning' sx={{ m: 1 }} onClick={handleClick}>
          {attachment.name}
        </Button>
      )}
      <Box flexGrow={1} />
      <IconButton color='inherit' onClick={handleClick}><ArchiveIcon /></IconButton>
    </Box>
  )
}

const ExpandableMessage: React.FC<{ message: Message }> = ({ message }) => {
  const theme = useTheme();
  const [expanded, setExpanded] = React.useState(false);
  const { userName, text, date, representerName, attachments } = message;
  const nameToShow = representerName ? `${userName} (rep. by ${representerName})` : userName;
  const hasAttachments = attachments.length > 0;
  const backgroundColor = message.userName.startsWith('Office') ? alpha(theme.palette.warning.main, 0.1) : theme.palette.background.paper;
  const unreadSx = message.read ? {} : { borderColor: theme.palette.warning.main, ':hover': { borderColor: theme.palette.warning.dark } };
  const attachmentDateTimeSx = {
    display: 'flex',
    flexDirection: 'column',
    padding: 1,
    position: 'absolute',
    top: 1,
    right: 1,
    alignItems: 'flex-end'
  }

  return (
    <ExpandableMessageContainer onClick={() => setExpanded(!expanded)} sx={{ backgroundColor, ...unreadSx }}>
      <MessageHeaderContainer>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <UserAvatar userName={userName} representerName={representerName} />
          <PaddedTypography fontWeight='bold'>{nameToShow}</PaddedTypography>
        </Box>
        <AttachmentAndDateTime date={date} hasAttachments={hasAttachments} sx={attachmentDateTimeSx} color='warning' />
      </MessageHeaderContainer>
      <PaddedTypography noWrap={!expanded}>{text}</PaddedTypography>
      {expanded && <MessageExpandedSection message={message} />}
    </ExpandableMessageContainer>
  )
}

const ThreadContainer: React.FC<{ thread: Thread }> = ({ thread }) => {
  const { messages, userName, topicName } = thread;
  const replyTo = userName;
  const regarding = topicName;
  const dialogTitle = `Reply to ${replyTo} regarding ${regarding}`;
  const [open, setOpen] = React.useState(false);

  return (
    <>
      <Box>
        {messages
          .sort((a, b) => a.date.getTime() - b.date.getTime())
          .map(message => <ExpandableMessage key={message.id} message={message} />)
        }
      </Box>
      <Button variant='contained' color='warning' sx={{ mx: 1, my: 2, color: 'white' }} startIcon={<ReplyIcon />} onClick={() => setOpen(true)}>Reply</Button>
      <Dialog open={open} onClose={() => setOpen(false)}>
        <DialogTitle>{dialogTitle}</DialogTitle>
        <DialogContent>
          <StyledTextArea minRows={10} placeholder='Write your reply here...' />
        </DialogContent>
        <StyledDialogActions>
          <Box>
            <Button onClick={() => setOpen(false)} variant='contained' sx={{ mr: 1, color: 'white' }} color='warning'>Send</Button>
            <Button onClick={() => setOpen(false)} color='warning'>Cancel</Button>
          </Box>
          <IconButton color='inherit'>
            <AttachFileIcon />
          </IconButton>
        </StyledDialogActions>
      </Dialog>
    </>
  )
}

const Messages: React.FC<{}> = () => {

  const thread = demoThreads[1];

  return (
    <Box sx={{ p: 1, pt: 2.5 }}>
      {thread ?
        <ThreadContainer thread={thread} /> :
        <>
          <Alert severity='info' color='warning' sx={{ my: 1 }}><FormattedMessage id='core.taskOps.workOnTask.messages.none' /></Alert>
          <Button variant='outlined' color='warning' startIcon={<MailOutlineIcon />} sx={{ textTransform: 'none' }}>
            <Typography><FormattedMessage id='core.taskOps.workOnTask.messages.start' /></Typography>
          </Button>
        </>
      }
    </Box>
  )
}

const Menu: React.FC<{}> = () => {
  const { activeTab, withTab } = TaskClient.useMenu();

  const getVariant = (tab: string) => {
    return activeTab === tab ? 'contained' : 'outlined';
  }

  return (
    <Stack spacing={1} direction='row'>
      <Button startIcon={<ForumIcon />} color='warning' variant={getVariant('messages')} sx={activeTab === 'messages' ? { color: 'white' } : {}} onClick={() => withTab('messages')}>
        <Typography sx={{ textTransform: 'none' }}><FormattedMessage id='core.taskOps.workOnTask.menu.messages' /></Typography>
      </Button>
      <Button startIcon={<AttachEmailIcon />} color='info' variant={getVariant('attachments')} onClick={() => withTab('attachments')}>
        <Typography sx={{ textTransform: 'none' }}><FormattedMessage id='core.taskOps.workOnTask.menu.attachments' /></Typography>
      </Button>
      <Button startIcon={<AssignmentTurnedInIcon />} variant={getVariant('checklists')} onClick={() => withTab('checklists')}>
        <Typography sx={{ textTransform: 'none' }}><FormattedMessage id='core.taskOps.workOnTask.menu.checklists' /></Typography>
      </Button>
    </Stack>
  )
}

const CloseDialogButton: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  return (
    <IconButton onClick={onClose}>
      <CloseIcon />
    </IconButton>
  )
}

const Fields = { Title, Description, Checklist, StartDate, DueDate, Attachments, Form, Messages, Menu, CloseDialogButton }
export default Fields;