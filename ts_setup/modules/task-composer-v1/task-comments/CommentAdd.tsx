import React from 'react';
import { OutlinedInput, Grid2, Stack, Button } from '@mui/material';

import { FormattedMessage } from 'react-intl';

import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';


type CommentAddProps = {
  parentComment?: TaskApi.Comment
  task: TaskApi.Task
  onAdded: () => void
  onCancel: () => void
  isExternalThread?: boolean
}

export const CommentAdd: React.FC<CommentAddProps> = (props) => {
  let input: HTMLTextAreaElement | null = null;
  const [inputValue, setInputValue] = React.useState<string | null>(null);
  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => setInputValue(event.target.value);
  const backend = useTaskBackend();

  const send = () => {
    const { parentComment, task, onAdded, isExternalThread } = props;
    if (!input || !input.value) return;
    const replyToId = parentComment?.id;
    backend.persistence.createOneComment(input.value, replyToId, task, isExternalThread)
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
