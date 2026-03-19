import React from 'react';
import { TextField, Typography, Divider } from '@mui/material';
import { FsDirentCommentsProps } from './FsDirentCommentsProps';
import { useUtilityClasses, FsDirentCommentsRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';
import { useIntl } from 'react-intl';

export const FsDirentComments: React.FC<FsDirentCommentsProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const { dirent } = props;
  const classes = useUtilityClasses();
  const [newComment, setNewComment] = React.useState('');

  const comments = dirent?.comments || [];

  return (
    <FsDirentCommentsRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>Comments ({comments.length})</Typography>

      <TextField fullWidth multiline className={classes.textField}
        placeholder={intl.formatMessage({ id: 'fs.direntComments.commentField.placeholder' })}
        value={newComment}
        onChange={(e) => setNewComment(e.target.value)}
        minRows={2}
        maxRows={10}
      />
      <div className={classes.spacer} />

      <div className={classes.commentsContainer}>
        {comments.length === 0 ? (
          <Typography className={classes.noComments}>No comments yet.</Typography>
        ) : (
          comments.map((comment, index) => (
            <div key={index}>
              {index > 0 && <Divider className={classes.divider} />}
              <div className={classes.commentItem}>
                <Typography className={classes.commentContent}>{comment.comment}</Typography>
                <div className={classes.commentMeta}>
                  <Typography>{comment.author} • {comment.created}</Typography>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </FsDirentCommentsRoot>
  );
};