import React from 'react';
import { List } from '@mui/material';

import { TaskApi } from '../api-task';
import { CommentLocal } from './CommentLocal';

type Props = {
  comments: TaskApi.Comment[]
  task: TaskApi.Task
  isExternalThread?: boolean
  setReply: React.Dispatch<React.SetStateAction<boolean>>
}

export const Thread: React.FC<Props> = ({ comments, task, isExternalThread, setReply }) => {
  return (
    <List component='div' dense>
      {comments.map((comment) => (
        <CommentLocal key={comment.id} comment={comment} task={task} isExternalThread={isExternalThread} setReply={setReply}>
          {comment.__children && <Thread key={`${comment.id}-thread`} setReply={setReply} comments={comment.__children} task={task} isExternalThread={isExternalThread} />}
        </CommentLocal>
      ))}
    </List>
  );
}
