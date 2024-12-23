import React, { useState, useContext } from 'react';
import { OutlinedInput, Grid2, Stack } from '@mui/material';

import { TaskBackendContext } from '../../context/TaskApiConfigContext';
import { Comment } from '../../types/task/Comment';
import { Task } from '../../types/task/Task';

import * as Burger from '@/burger';

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
  const backendContext = useContext(TaskBackendContext);

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => setInputValue(event.target.value);

  const send = () => {
    const { parentComment, task, onAdded, isExternalThread } = props;
    if (!input || !input.value) return;
    const replyToId = parentComment?.id;
    backendContext.saveComment(input.value, replyToId, task, isExternalThread)
      .then(() => onAdded());
  };

  return (
    <>
      <Grid2 size={{ xs: 12 }}>
        <OutlinedInput fullWidth inputRef={ref => input = ref} multiline autoFocus onChange={handleChange} margin='dense' />
      </Grid2>
      <Grid2 size={{ xs: 12 }} sx={{ margin: 'auto' }}>
        <Stack direction='row' spacing={2}>
          <Burger.PrimaryButton onClick={send} disabled={!inputValue} label={'comment.store'} />
          <Burger.SecondaryButton onClick={props.onCancel} label='taskButton.cancel' />
        </Stack>
      </Grid2>
    </>
  );
}
