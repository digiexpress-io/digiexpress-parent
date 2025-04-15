import React, { useEffect, useRef, useState, } from 'react';
import { Box, Grid2, Button } from '@mui/material';


import { CommentAdd } from './CommentAdd';
import mapNestedEntities from './mapNestedEntities';
import { Thread } from './Thread';
import { FormattedMessage } from 'react-intl';
import { TaskApi } from '../api-task';


export type EveliTaskCommentsProps = {
  task: TaskApi.Task
  isExternalThread?: boolean,
  comments: TaskApi.Comment[],
  loadData: () => void,
  isThreaded?: boolean
  onCommentPosted?: () => void;
}

export const EveliTaskComments: React.FC<EveliTaskCommentsProps> = ({ task, isExternalThread, comments, loadData, isThreaded, onCommentPosted }) => {
  const [writingComment, setWritingComment] = useState(false);
  const [reply, setReply] = useState(false);
  const listRef = useRef<HTMLDivElement>(null);

  const toggleComment = () => {
    setWritingComment(!writingComment);
  }

  useEffect(() => {
    if (!reply && listRef.current) {
      const listContainer = listRef.current;
      listContainer.scrollTo({
        top: listContainer.scrollHeight - listContainer.clientHeight,
        behavior: 'smooth',
      });
    }
  }, [reply, comments])

  const handleCommentAdding = () => {
    setReply(false);
    loadData(); 
    onCommentPosted?.();
    toggleComment();
  }

  const getThread = (value: TaskApi.Comment[], task: TaskApi.Task) => {
    if (!task) return null;
    let comments = value;
    if (typeof isExternalThread !== 'undefined') {
      comments = comments.filter(comment => !!comment.external === isExternalThread);
    }
    comments = mapNestedEntities(
      comments,
      'id',
      'replyToId'
    );
    return (
      <Thread comments={comments} task={task}
        loadData={loadData}
        isExternalThread={isExternalThread}
        isThreaded={isThreaded}
        setReply={setReply}
      />);
  }

  const thread = getThread(comments, task);
  if (!task.id) {
    return null;
  }
  let buttonId = 'comment.add';
  if (typeof isExternalThread !== 'undefined') {
    if (isExternalThread) {
      buttonId = 'comment.addExternal';
    }
    else {
      buttonId = 'comment.addInternal';
    }
  }
  return (
    <Box p={1}>
      <Box style={{ maxHeight: '40vh', overflow: 'auto' }} ref={listRef}>
        {thread}
      </Box>
      <Grid2 container spacing={1}>
        {writingComment && <CommentAdd task={task}
          onAdded={handleCommentAdding} onCancel={toggleComment} isExternalThread={isExternalThread}  />}
        {!writingComment && (
          <Grid2 size={{ xs: 12 }}>
            <Button variant='contained' onClick={toggleComment}><FormattedMessage id={buttonId}/></Button>
          </Grid2>
        )}
      </Grid2>
    </Box>
  );
}