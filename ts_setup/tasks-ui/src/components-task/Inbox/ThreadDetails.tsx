import React from "react";

import {
  Box, Button, Typography, styled,
  Avatar, IconButton, List, ListItem, Paper, Dialog,
  DialogTitle, DialogContent, DialogActions, TextareaAutosize, useTheme, alpha
} from "@mui/material";
import ArchiveIcon from '@mui/icons-material/Archive';
import SupervisedUserCircleIcon from '@mui/icons-material/SupervisedUserCircle';
import ReplyIcon from '@mui/icons-material/Reply';
import AttachFileIcon from '@mui/icons-material/AttachFile';
import CallMadeIcon from '@mui/icons-material/CallMade';
import CallReceivedIcon from '@mui/icons-material/CallReceived';
import DownloadIcon from '@mui/icons-material/Download';
import DeleteIcon from '@mui/icons-material/Delete';

import { Message, TabType, Thread } from "./thread-types"
import { AttachmentAndDateTime } from "./ThreadPreview";
import { TaskExtension } from "descriptor-task";



const PaddedTypography = styled(Typography)(({ theme }) => ({
  padding: theme.spacing(1),
}))

const StyledHeader = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  borderBottom: '1px solid',
  borderColor: theme.palette.divider,
  justifyContent: 'space-between',
  width: '100%',
  padding: theme.spacing(1),
}))

const StyledMenuButton = styled(Button)(({ theme }) => ({
  borderRadius: theme.spacing(4),
  padding: theme.spacing(1),
}))

const StyledListItem = styled(ListItem)(({ theme }) => ({
  borderTop: '1px solid',
  borderBottom: '1px solid',
  borderColor: theme.palette.divider,
}))

const StyledTextArea = styled(TextareaAutosize)(({ theme }) => ({
  fontFamily: theme.typography.fontFamily,
  fontSize: theme.typography.fontSize,
  fontWeight: theme.typography.fontWeightRegular,
  padding: theme.spacing(1),
  borderRadius: '12px 12px 0 12px',
  width: '30vw',
}))

const StyledDialogActions = styled(DialogActions)(({ theme }) => ({
  display: 'flex',
  justifyContent: 'space-between',
  marginLeft: theme.spacing(2),
  marginRight: theme.spacing(1),
  marginBottom: theme.spacing(2),
}))

const ListItemContainer = styled(Box)({
  display: 'flex',
  flexDirection: 'row',
  justifyContent: 'space-between',
  width: '100%',
  alignItems: 'center'
})

const ExpandableMessageContainer = styled(Box)(({ theme }) => ({
  borderTop: '1px solid',
  borderBottom: '1px solid',
  borderColor: theme.palette.divider,
  ':hover': {
    cursor: 'pointer',
    borderColor: theme.palette.primary.main,
  }
}))

const MessageHeaderContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'row',
  justifyContent: 'space-between',
  position: 'relative',
}))

const ThreadDetailsMenu: React.FC<{
  formName: string,
  activeTab: TabType,
  setActiveTab: React.Dispatch<React.SetStateAction<TabType>>
}> = ({ formName, activeTab, setActiveTab }) => {
  const handleTabClick = (tab: TabType) => () => setActiveTab(tab);
  const buttonVariant = (tab: TabType) => activeTab === tab ? 'contained' : 'outlined';

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', p: 1, '.MuiButton-root': { mr: 1 } }}>
      <StyledMenuButton variant={buttonVariant('messages')} onClick={handleTabClick('messages')}>Threads</StyledMenuButton>
      <StyledMenuButton variant={buttonVariant('attachments')} onClick={handleTabClick('attachments')}>Attachments</StyledMenuButton>
      <StyledMenuButton variant={buttonVariant('form')} onClick={handleTabClick('form')}>{formName}</StyledMenuButton>
    </Box>
  )
}

const UserAvatar: React.FC<{ userName: string, representerName?: string }> = ({ userName, representerName }) => {
  const avatarText = userName.split(' ').map(name => name[0]).join('').toUpperCase();
  const avatarIcon = <SupervisedUserCircleIcon fontSize='large' />;
  const avatarColor = userName === 'Office Worker' ? 'success.main' : 'primary.main';

  return (
    <Avatar sx={{ bgcolor: avatarColor, m: 1 }} >{representerName ? avatarIcon : avatarText}</Avatar>
  )
}

const ExpandableMessage: React.FC<{ message: Message }> = ({ message }) => {
  const [expanded, setExpanded] = React.useState(false);
  const { userName, text, date, representerName, attachments } = message;
  const nameToShow = representerName ? `${userName} (rep. by ${representerName})` : userName;
  const hasAttachments = attachments.length > 0;
  const unreadSx = message.read ? {} : { borderColor: 'primary.main', ':hover': { borderColor: 'primary.dark' } };
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
    <ExpandableMessageContainer onClick={() => setExpanded(!expanded)} sx={unreadSx}>
      <MessageHeaderContainer>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <UserAvatar userName={userName} representerName={representerName} />
          <PaddedTypography fontWeight='bold'>{nameToShow}</PaddedTypography>
        </Box>
        <AttachmentAndDateTime date={date} hasAttachments={hasAttachments} sx={attachmentDateTimeSx} />
      </MessageHeaderContainer>
      <PaddedTypography noWrap={!expanded}>{text}</PaddedTypography>
      {expanded && <MessageExpandedSection message={message} />}
    </ExpandableMessageContainer>
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
        <Button key={attachment.id} startIcon={<DownloadIcon />} variant='outlined' sx={{ m: 1 }} onClick={handleClick}>
          {attachment.name}
        </Button>
      )}
      <Box flexGrow={1} />
      <IconButton color='inherit' onClick={handleClick}><ArchiveIcon /></IconButton>
    </Box>
  )
}


const AttachmentListItem: React.FC<{ attachment: TaskExtension }> = ({ attachment }) => {
  const theme = useTheme();
  const backgroundColor = attachment.id.includes('2') ? theme.palette.background.paper : alpha(theme.palette.primary.main, 0.1);
  const icon = attachment.id.includes('2') ? <CallMadeIcon color='primary' /> : <CallReceivedIcon color='primary' />;
  return (
    <StyledListItem sx={{ backgroundColor }}>
      <ListItemContainer>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          {icon}
          <Box>
            <PaddedTypography>{attachment.externalId}</PaddedTypography>
            <PaddedTypography>{attachment.created}</PaddedTypography>
          </Box>
        </Box>
        <Box>
          <IconButton>
            <DownloadIcon color='primary' />
          </IconButton>
          <IconButton>
            <DeleteIcon color='error' />
          </IconButton>
        </Box>
      </ListItemContainer>
    </StyledListItem>
  )
}

const MessagesTab: React.FC<{ thread: Thread }> = ({ thread }) => {
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
      <Button variant='contained' sx={{ mx: 1, my: 2 }} startIcon={<ReplyIcon />} onClick={() => setOpen(true)}>Reply</Button>
      <Dialog open={open} onClose={() => setOpen(false)}>
        <DialogTitle>{dialogTitle}</DialogTitle>
        <DialogContent>
          <StyledTextArea minRows={10} placeholder='Write your reply here...' />
        </DialogContent>
        <StyledDialogActions>
          <Box>
            <Button onClick={() => setOpen(false)} variant='contained' sx={{ mr: 1 }}>Send</Button>
            <Button onClick={() => setOpen(false)}>Cancel</Button>
          </Box>
          <IconButton color='inherit'>
            <AttachFileIcon />
          </IconButton>
        </StyledDialogActions>
      </Dialog>
    </>
  )
}

const AttachmentsTab: React.FC<{ attachments: TaskExtension[] }> = ({ attachments }) => {
  return (
    <List sx={{ p: 0 }}>
      {attachments.map(attachment => <AttachmentListItem key={attachment.id} attachment={attachment} />)}
    </List>
  )
}

const FormTab: React.FC<{ formName: string }> = ({ formName }) => {
  return (
    <Paper sx={{ m: 1, pl: 2, pt: 2, pb: 50 }}>
      {formName}
    </Paper>
  )
}

const ThreadDetails: React.FC<{ thread: Thread }> = ({ thread }) => {
  const { userName, topicName, formName } = thread;
  const [activeTab, setActiveTab] = React.useState<TabType>('messages');
  const allAttachments = thread.messages.flatMap(message => message.attachments);

  return (
    <>
      <StyledHeader>
        <PaddedTypography fontWeight='bold'>{userName} - {topicName}</PaddedTypography>
        <IconButton color='inherit'><ArchiveIcon /></IconButton>
      </StyledHeader>
      <ThreadDetailsMenu formName={formName} activeTab={activeTab} setActiveTab={setActiveTab} />
      {activeTab === 'messages' && <MessagesTab thread={thread} />}
      {activeTab === 'attachments' && <AttachmentsTab attachments={allAttachments} />}
      {activeTab === 'form' && <FormTab formName={formName} />}
    </>
  )
}

export default ThreadDetails;
