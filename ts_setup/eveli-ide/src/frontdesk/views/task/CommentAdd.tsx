import React, { useState, useContext } from 'react';
import { OutlinedInput, Grid2, Stack, Button } from '@mui/material';

import { FormattedMessage } from 'react-intl';


import { Comment } from '../../types/task/Comment';
import { Task } from '../../types/task/Task';
import { useFetch } from '@dxs-ts/eveli-fetch';


type CommentAddProps = {
  parentComment?: Comment
  task: Task
  onAdded: () => void
  onCancel: () => void
  isExternalThread?: boolean
}

export const CommentAdd: React.FC<CommentAddProps> = (props) => {
  let input: HTMLTextAreaElement | null = null;
  const [inputValue, setInputValue] = useState<string | null>(null);
  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => setInputValue(event.target.value);
  const { saveComment } = useFetch('worker/rest/api/tasks/$taskId/comments.POST', {});

  const send = () => {
    const { parentComment, task, onAdded, isExternalThread } = props;
    if (!input || !input.value) return;
    const replyToId = parentComment?.id;
    saveComment(input.value, replyToId, task, isExternalThread)
      .then(() => onAdded());
  };

  return (
    <>
      <Grid2 size={{ xs: 12 }}>
        <OutlinedInput fullWidth inputRef={ref => input = ref} multiline autoFocus onChange={handleChange} margin='dense' />
      </Grid2>
      <Grid2 size={{ xs: 12 }} sx={{ margin: 'auto' }}>
        <Stack direction='row' spacing={2}>
          <Button variant='contained' onClick={send} disabled={!inputValue}><FormattedMessage id='comment.store'/></Button>
          <Button onClick={props.onCancel} variant='text'><FormattedMessage id='taskButton.cancel'/></Button>
        </Stack>
      </Grid2>
    </>
  );
}
