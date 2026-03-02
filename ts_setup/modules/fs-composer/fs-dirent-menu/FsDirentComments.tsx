import React from 'react';
import { Box, TextField, Typography, Divider } from '@mui/material';
import { FsNode, useFs } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';
import { FsColors } from '../fs-theme';

export interface FsDirentCommentsProps {
  node: FsNode | undefined;
}

export const FsDirentComments: React.FC<FsDirentCommentsProps> = (props) => {
  const classes = useUtilityClasses();
  const { isDarkMode } = useFs();
  const [newComment, setNewComment] = React.useState('');

  const comments = props.node?.comments || [];

  return (
    <Box>
      <Typography variant='caption' fontWeight={500} sx={{ color: isDarkMode ? FsColors.dark.text : FsColors.light.text }}>Comments ({comments.length})</Typography>

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
          <Typography variant='caption' sx={{ color: isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary, fontStyle: 'italic' }}>
            No comments yet.
          </Typography>
        ) : (
          comments.map((comment, index) => (
            <Box key={index}>
              {index > 0 && <Divider sx={{ borderColor: isDarkMode ? FsColors.dark.border : FsColors.light.border }} />}
              <Box sx={{ mb: 1 }}>
                <Typography variant='caption' sx={{ color: isDarkMode ? FsColors.dark.text : FsColors.light.text }}>
                  {comment.comment}
                </Typography>
                <Box justifySelf='flex-end'>
                  <Typography variant='caption' sx={{ color: isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary, fontStyle: 'italic' }}>
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