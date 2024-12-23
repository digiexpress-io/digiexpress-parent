import React, { useEffect, useRef, useState } from 'react';
import { Box, Grid2 } from '@mui/material';

import { Task } from '../../types/task/Task';
import { Comment as CommentData } from '../../types/task/Comment';
import { CommentAdd } from './CommentAdd';
import mapNestedEntities from '../../util/mapNestedEntities';
import { Thread } from './Thread';


import * as Burger from '@/burger';

type CommentThreadProps = {
  task: Task
  isExternalThread?: boolean,
  comments: CommentData[],
  loadData: () => void,
  isThreaded?: boolean
}

export const CommentThread: React.FC<CommentThreadProps> = ({ task, isExternalThread, comments, loadData, isThreaded }) => {
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
    toggleComment();
  }

  const getThread = (value: CommentData[], task: Task) => {
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
          onAdded={handleCommentAdding} onCancel={toggleComment} isExternalThread={isExternalThread} />}
        {!writingComment && (
          <Grid2 size={{ xs: 12 }}>
            <Burger.PrimaryButton onClick={toggleComment} label={buttonId} />
          </Grid2>
        )}
      </Grid2>
    </Box>
  );
}
