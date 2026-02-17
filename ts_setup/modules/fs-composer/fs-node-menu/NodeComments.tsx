import React from 'react';
import { Box, TextField, Typography, Divider } from '@mui/material';
import { FsNode } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';

export interface NodeCommentsProps {
  node: FsNode | undefined;
}

export const NodeComments: React.FC<NodeCommentsProps> = (props) => {
  const classes = useUtilityClasses();
  const [newComment, setNewComment] = React.useState('');

  const comments = props.node?.comments || [];

  return (
    <Box>
      <Typography variant='caption' fontWeight={500} sx={{ color: '#cccccc' }}>Comments ({comments.length})</Typography>

      <TextField className={classes.textField} placeholder='Add a comment...'
        value={newComment}
        onChange={(e) => setNewComment(e.target.value)}
        fullWidth
        multiline
        minRows={2}
        maxRows={10}
      />
      <Box mb={0.5} />

      <Box>
        {comments.length === 0 ? (
          <Typography variant='caption' sx={{ color: '#888888', fontStyle: 'italic' }}>
            No comments yet.
          </Typography>
        ) : (
          comments.map((comment, index) => (
            <Box key={index}>
              {index > 0 && <Divider sx={{ borderColor: '#3c3c3c' }} />}
              <Box sx={{ mb: 1 }}>
                <Typography variant='caption' sx={{ color: '#cccccc' }}>
                  {comment.comment}
                </Typography>
                <Box justifySelf='flex-end'>
                  <Typography variant='caption' sx={{ color: '#888888', fontStyle: 'italic' }}>
                    {comment.author} • {comment.created}
                  </Typography>
                </Box>
              </Box>
            </Box>
          ))
        )}
      </Box>
    </Box>
  );
};