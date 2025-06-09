import React, { useEffect, useRef, useState, } from 'react';
import { Box, Grid2, Button } from '@mui/material';


import { CommentAdd } from './CommentAdd';
import mapNestedEntities from './mapNestedEntities';
import { Thread } from './Thread';
import { FormattedMessage } from 'react-intl';
import { TaskApi } from '../api-task';
import { useSnackbar } from 'notistack';


export type EveliTaskCommentsProps = {
  task: TaskApi.Task
  isExternalThread: boolean,
  reload: () => void
}

export const EveliTaskComments: React.FC<EveliTaskCommentsProps> = ({ task, isExternalThread, reload }) => {
  const { enqueueSnackbar } = useSnackbar();
  const [writingComment, setWritingComment] = useState(false);
  const [reply, setReply] = useState(false);
  const listRef = useRef<HTMLDivElement>(null);
  const { comments } = task;

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
    enqueueSnackbar(<FormattedMessage id="task.comments.external.added" />, { variant: 'success' })
    reload();
    toggleComment();
  }

  const getThread = (value: TaskApi.Comment[]) => {
    let comments = value;
    if (typeof isExternalThread !== 'undefined') {
      comments = comments.filter(comment => !!comment.external === isExternalThread);
    }
    comments = mapNestedEntities(
      comments,
      'id',
      'replyToId'
    ).sort((a,b)=> {if (a.created > b.created) return 1;if (a.created < b.created) return -1; return 0;});
    return (
      <Thread comments={comments} task={task}
        isExternalThread={isExternalThread}
        setReply={setReply}
      />);
  }

  const thread = getThread(comments);
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
        {writingComment && <CommentAdd task={task} onAdded={handleCommentAdding} onCancel={toggleComment} isExternalThread={isExternalThread}  />}
        {!writingComment && (
          <Grid2 size={{ xs: 12 }}>
            <Button variant='contained' onClick={toggleComment}><FormattedMessage id={buttonId}/></Button>
          </Grid2>
        )}
      </Grid2>
    </Box>
  );
}