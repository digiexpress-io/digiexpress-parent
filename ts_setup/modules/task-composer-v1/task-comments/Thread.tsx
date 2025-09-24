import React from 'react';
import { List } from '@mui/material';

import { TaskApi } from '@dxs-ts/task-api';
import { TaskCommentBody } from './TaskCommentBody';

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
        <TaskCommentBody key={comment.id} comment={comment} task={task} isExternalThread={isExternalThread} setReply={setReply}>
          {comment.__children && <Thread key={`${comment.id}-thread`} setReply={setReply} comments={comment.__children} task={task} isExternalThread={isExternalThread} />}
        </TaskCommentBody>
      ))}
    </List>
  );
}
