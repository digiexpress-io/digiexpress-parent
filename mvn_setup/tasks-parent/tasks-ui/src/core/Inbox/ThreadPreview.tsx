import React from "react";

import { Box, Typography, styled, TableCell, IconButton, SxProps } from "@mui/material";
import AttachFileIcon from '@mui/icons-material/AttachFile';
import ArchiveIcon from '@mui/icons-material/Archive';
import MarkAsUnreadIcon from '@mui/icons-material/MarkAsUnread';
import SupervisedUserCircleIcon from '@mui/icons-material/SupervisedUserCircle';

import { ThreadPreviewProps } from "./thread-types";

const StyledTableCell = styled(TableCell)({
  border: 'none',
})

const StyledAttachmentAndDateTimeContainer = styled(StyledTableCell)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'row',
  alignItems: 'center',
  justifyContent: 'flex-end'
}))

const StyledThreadPreviewContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  borderTop: '1px solid',
  borderBottom: '1px solid',
  borderColor: theme.palette.divider,
  justifyContent: 'space-between',
  width: '100%',
  position: 'relative',
  ':hover': {
    cursor: 'pointer',
    borderColor: theme.palette.primary.main,
  },
}))

const ThreadPreviewActionsContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  backgroundColor: theme.palette.background.default,
  padding: theme.spacing(1),
  position: 'absolute',
  right: 0,
  top: '15%',
  zIndex: 1,
}))

const ThreadPreviewActions: React.FC<{ read: boolean }> = ({ read }) => {
  const unreadSx = { backgroundColor: read ? 'background.default' : '#e4eaf5' };

  const handleClick = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.stopPropagation();
  }

  return (
    <ThreadPreviewActionsContainer sx={unreadSx}>
      <IconButton sx={{ mr: 1 }} onClick={handleClick}><ArchiveIcon /></IconButton>
      <IconButton onClick={handleClick}><MarkAsUnreadIcon /></IconButton>
    </ThreadPreviewActionsContainer>
  )
}

const AttachmentAndDateTime: React.FC<{ hasAttachments: boolean, date: Date, sx?: SxProps }> = ({ hasAttachments, date, sx }) => {
  return (
    <StyledAttachmentAndDateTimeContainer width='20%'>
      {hasAttachments && <AttachFileIcon color='primary' sx={{ mr: 1 }} />}
      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end' }}>
        <Typography>{date.toLocaleDateString('en-US', { day: '2-digit', month: '2-digit', year: '2-digit' })}</Typography>
        <Typography>{date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hourCycle: 'h24' })}</Typography>
      </Box>
    </StyledAttachmentAndDateTimeContainer>
  )
}

const ThreadPreview: React.FC<ThreadPreviewProps> = (props) => {
  const { thread, onClick } = props;
  const { topicName, messages } = thread;
  const lastMessage = messages[messages.length - 1];
  const { userName, text, date, read, representerName } = lastMessage;
  const hasAttachments = messages.some(message => message.attachments.length > 0);
  const newMessageSx = read ? {} : { backgroundColor: '#e4eaf5', '& .MuiTypography-root': { fontWeight: 'bold' } };

  const [hovering, setHovering] = React.useState(false);

  const onMouseOver = () => setHovering(true);
  const onMouseOut = () => setHovering(false);

  return (
    <StyledThreadPreviewContainer sx={newMessageSx} onClick={() => onClick(thread)} onMouseOver={onMouseOver} onMouseOut={onMouseOut}>
      <StyledTableCell width='20%'>
        <Typography display='flex' alignItems='center'>
          {userName}{representerName && <SupervisedUserCircleIcon color='primary' sx={{ ml: 1 }} />}
        </Typography>
      </StyledTableCell>
      <StyledTableCell width='60%'><Typography noWrap>{topicName} - {text}</Typography></StyledTableCell>
      <AttachmentAndDateTime hasAttachments={hasAttachments} date={date} sx={{ width: '20%' }} />
      {hovering && <ThreadPreviewActions read={read} />}
    </StyledThreadPreviewContainer >
  )
}

export { AttachmentAndDateTime };
export default ThreadPreview;
