import React from "react";

import { Box, Button, Typography, styled, Avatar, IconButton, List, ListItem, Paper } from "@mui/material";
import ArchiveIcon from '@mui/icons-material/Archive';
import SupervisedUserCircleIcon from '@mui/icons-material/SupervisedUserCircle';
import ReplyIcon from '@mui/icons-material/Reply';
import AttachFileIcon from '@mui/icons-material/AttachFile';
import DownloadIcon from '@mui/icons-material/Download';
import DeleteIcon from '@mui/icons-material/Delete';

import { Attachment, Message, TabType, Thread } from "./thread-types"
import { AttachmentAndDateTime } from "./ThreadPreview";

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

const ListItemContainer = styled(Box)({
  display: 'flex',
  flexDirection: 'row',
  justifyContent: 'space-between',
  width: '100%',
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
    <ExpandableMessageContainer onClick={() => setExpanded(!expanded)}>
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


const AttachmentListItem: React.FC<{ attachment: Attachment }> = ({ attachment }) => {
  return (
    <StyledListItem>
      <ListItemContainer>
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <AttachFileIcon color='primary' />
          <PaddedTypography>{attachment.name}</PaddedTypography>
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

const MessagesTab: React.FC<{ messages: Message[] }> = ({ messages }) => {
  return (
    <>
      <Box>
        {messages
          .sort((a, b) => a.date.getTime() - b.date.getTime())
          .map(message => <ExpandableMessage key={message.id} message={message} />)
        }
      </Box>
      <Button variant='contained' sx={{ m: 1 }} startIcon={<ReplyIcon />}>Reply</Button>
    </>
  )
}

const AttachmentsTab: React.FC<{ attachments: Attachment[] }> = ({ attachments }) => {
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
      {activeTab === 'messages' && <MessagesTab messages={thread.messages} />}
      {activeTab === 'attachments' && <AttachmentsTab attachments={allAttachments} />}
      {activeTab === 'form' && <FormTab formName={formName} />}
    </>
  )
}

export default ThreadDetails;
