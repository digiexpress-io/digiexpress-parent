import React, { useState } from 'react';
import { FormattedDate, useIntl } from 'react-intl';
import { Comment as CommentData, CommentSource } from '../../types/task/Comment';
import {
  ListItem,
  Link,
  Typography,
  Grid,
  Box
} from '@mui/material';
import { Task } from '../../types/task/Task';
import { toZonedTime } from 'date-fns-tz';
import { alpha } from "@mui/material";
import { AddComment } from './AddComment';

type Props = {
  comment: CommentData
  task: Task
  loadData:()=>void
  isExternalThread?: boolean
  isThreaded?: boolean
  setReply: React.Dispatch<React.SetStateAction<boolean>>
}

export const LocalComment:React.FC<React.PropsWithChildren<Props>> =
  ({comment, task, loadData, children, isExternalThread, isThreaded, setReply}) => 
{
  const [writingReply, setWritingReply] = useState(false);
  const now = new Date();
  const {formatMessage} = useIntl();

  const toggleReply = () => {
    setWritingReply(!writingReply);
  };

  const handleCommentAdd = () => {
    setReply(true);
    loadData();
    setWritingReply(false);
  }

  function getCommentAlignment(){
    if(isExternalThread && comment.source && comment.source === CommentSource.FRONTDESK){
      return "right";
    }
    return "left";
  }

  let createdTime = comment.created;
  const created = new Date(createdTime);
  const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
  const zonedDate = toZonedTime(created, timeZone);
  const showYear = zonedDate.getFullYear() !== now.getFullYear();
  const header = (
    <Typography variant="caption">
      <Box sx={{ display: 'inline', fontWeight: 'bold', paddingLeft: '1em', paddingRight: '1em' }}>
        {comment.userName}
      </Box>
      <FormattedDate
        value={zonedDate.toUTCString()}
        year={(showYear && 'numeric') || undefined}
        month='long'
        day='numeric'
        hour='2-digit'
        minute='2-digit'
      />
    </Typography>
  )
  const body = (
    <React.Fragment>
      <Box sx={{ display: 'block'}}>
        <Box sx={{ display: 'inline-block', borderRadius: '16px', padding: '8px', bgcolor: theme=>alpha(theme.palette.info.light, 0.1) }}>
          <Typography variant="body1" component='span' sx={{whiteSpace: 'pre'}}>
            {comment.commentText}
          </Typography>
        </Box>
      </Box>
      {isThreaded &&
        <Link href="#" onClick={e=> {e.preventDefault();toggleReply()}}>
          {formatMessage({id:'comment.reply'})}
        </Link>
      }
    </React.Fragment>
  )
  return (
    <ListItem component='div' style={{display: 'block', textAlign: getCommentAlignment()}} dense>
      {header}
      {body}
      {writingReply && (
        <Grid container spacing={1}>
          <AddComment
            parentComment={comment}
            onAdded={handleCommentAdd}
            onCancel={toggleReply}
            task={task}
            isExternalThread={isExternalThread}
          />
        </Grid>
      )}
      {children}
    </ListItem>
  );
}
