import React from 'react';
import { Divider, TextField, Typography } from '@mui/material';
import { Comment, CommentSource } from '../../frontdesk/types/task/Comment';

import * as Burger from '@/burger';

export interface FeedbackOneTaskProps {
  taskId: string | undefined;
  workerReplies: Comment[];
}

export const FeedbackOneTask: React.FC<FeedbackOneTaskProps> = ({ taskId, workerReplies }) => {


  const frontdeskComments = workerReplies
    .filter(r => r.source === CommentSource.FRONTDESK)
    .map(r => r.commentText)
    .join('\n');

  const [comments, setComments] = React.useState<string>(frontdeskComments);

  React.useEffect(() => {
    setComments(frontdeskComments);
  }, [frontdeskComments]);

  function handlePublish() {
    console.log("Published comments:", comments);
  };

  return (
    <>
      <div style={{ display: 'flex', flexDirection: 'column', padding: 10 }}>
        <Typography variant='h1'>Public reply to customer feedback</Typography>
        <Typography variant='body2'>Feedback id: 13</Typography>
        <Typography variant='body2'>Source task id: {taskId}</Typography>
        <Typography variant='body2'>Form name: "Send feedback"</Typography>
        <Typography variant='body2'>Date feedback received from customer: "09/12/2024"</Typography>

        <Divider sx={{ mt: 2 }} />

        <Typography variant='h2'>Customer feedback</Typography>
        <Typography variant='body2'>Category: "Public parks",</Typography>
        <Typography variant='body2'>Sub-category: "Children's playground"</Typography>
        <Typography mt={2}>
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
          Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in
          reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in
          culpa qui officia deserunt mollit anim id est laborum."
        </Typography>

        <Typography mt={2}>My reply to customer</Typography>
        <TextField onChange={(e) => setComments(e.target.value)}
          sx={{ mb: 3 }}
          multiline
          minRows={4}
          placeholder='Write a reply here'
          value={comments}
        />
      </div>
      <Burger.SecondaryButton onClick={() => console.log('cancel')} label='button.cancel' />
      <Burger.PrimaryButton onClick={handlePublish} label='button.publish' />
    </>
  )
}