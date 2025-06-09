import React, { useState } from 'react';
import { ListItem, Link, Typography, Grid2, Box, alpha } from '@mui/material';
import { FormattedDate, useIntl } from 'react-intl';
import { toZonedTime } from 'date-fns-tz';


import { CommentAdd } from './CommentAdd';
import { TaskApi } from '../api-task';


type CommentLocalProps = {
  comment: TaskApi.Comment;
  children: React.ReactNode;
  task: TaskApi.Task;
  isExternalThread?: boolean;
  setReply: React.Dispatch<React.SetStateAction<boolean>>;
}


export const CommentLocal: React.FC<CommentLocalProps> =
  ({ comment, children, task, isExternalThread, setReply }) => {
    const [writingReply, setWritingReply] = useState(false);
    const now = new Date();

    const toggleReply = () => {
      setWritingReply(!writingReply);
    };

    const handleCommentAdd = () => {
      setReply(true);
      setWritingReply(false);
    }

    function getCommentAlignment() {
      if (isExternalThread && comment.source && comment.source === TaskApi.CommentSource.FRONTDESK) {
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
        <Box sx={{ display: 'block' }}>
          <Box sx={{ display: 'inline-block', borderRadius: '16px', padding: '8px', bgcolor: theme => alpha(theme.palette.info.light, 0.1) }}>
            <Typography variant="body1" component='span' sx={{ whiteSpace: 'pre' }}>
              {comment.commentText}
            </Typography>
          </Box>
        </Box>
      </React.Fragment>
    )
    return (
      <ListItem component='div' style={{ display: 'block', textAlign: getCommentAlignment() }} dense>
        {header}
        {body}
        {writingReply && (
          <Grid2 container spacing={1}>
            <CommentAdd
              parentComment={comment}
              onAdded={handleCommentAdd}
              onCancel={toggleReply}
              task={task}
              isExternalThread={isExternalThread}
            />
          </Grid2>
        )}
        {children}
      </ListItem>
    );
  }
