import React from 'react';
import { List } from '@mui/material';

import { TaskApi } from '../api-task';
import { CommentLocal } from './CommentLocal';

type Props = {
  comments: TaskApi.Comment[]
  task: TaskApi.Task
  loadData: ()=>void
  isExternalThread?: boolean
  isThreaded?: boolean
  setReply: React.Dispatch<React.SetStateAction<boolean>>
}

export const Thread: React.FC<Props> = ({comments, task, loadData, isExternalThread, isThreaded, setReply}) => {
  return (
    <List component='div' dense>
      {comments.map((comment) => (
        <CommentLocal key={comment.id} comment={comment} task={task} 
          loadData={loadData} isExternalThread={isExternalThread} isThreaded={isThreaded} setReply={setReply}>
          {comment.__children && <Thread setReply={setReply} comments={comment.__children} task={task} loadData={loadData} 
            isExternalThread={isExternalThread} isThreaded={isThreaded}/>}
        </CommentLocal>
      ))}
    </List>
  );
}
