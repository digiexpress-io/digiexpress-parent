import React, { useState } from 'react';
import { Typography, Grid2, generateUtilityClass, styled, useThemeProps } from '@mui/material';
import EmailOutlinedIcon from '@mui/icons-material/EmailOutlined';
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined';
import composeClasses from '@mui/utils/composeClasses';
import { DateTime } from 'luxon';

import { TaskApi } from '@dxs-ts/task-api';

import { CommentAdd } from './CommentAdd';



type TaskCommentBodyProps = {
  comment: TaskApi.Comment;
  children: React.ReactNode;
  task: TaskApi.Task;
  isExternalThread?: boolean;
  setReply: React.Dispatch<React.SetStateAction<boolean>>;
}


export const TaskCommentBody: React.FC<TaskCommentBodyProps> = (initProps) => {
  const classes = useUtilityClasses();
  const [writingReply, setWritingReply] = useState(false);

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { comment, children, task, isExternalThread, setReply } = props;


  const toggleReply = () => {
    setWritingReply(!writingReply);
  };

  const handleCommentAdd = () => {
    setReply(true);
    setWritingReply(false);
  }
  const createdDate = DateTime.fromISO(comment.created).setLocale('fi').toFormat('dd.MM.yyyy HH:mm');



  return (
    <TaskCommentBodyRoot className={classes.root} ownerState={props}>

      <div className={classes.header}>
        {isExternalThread ? <EmailOutlinedIcon className={classes.icon} /> : <DescriptionOutlinedIcon className={classes.icon} />}
        <Typography component='span'>{comment.userName}</Typography>
        <Typography component='span'>{createdDate}</Typography>
      </div>

      <div className={classes.body}>
        <Typography
          sx={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}
        >
          {comment.commentText}
        </Typography>
      </div>

      {writingReply && (
        <Grid2 container spacing={1}>
          <CommentAdd
            parentComment={comment}
            onAdded={handleCommentAdd}
            onCancel={toggleReply}
            task={task}
            isExternalThread={isExternalThread}
          />
        </Grid2>
      )}
      {children}
    </TaskCommentBodyRoot>
  );
}

export const MUI_NAME = 'TaskCommentBody';
export interface TaskCommentBodyClasses {
  root: string;
}

export type TaskCommentBodyClassKey = keyof TaskCommentBodyClasses;


export const TaskCommentBodyRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      styles.header,
      styles.body,
      styles.icon,
    ];
  },
})<{ ownerState: TaskCommentBodyProps }>(({ theme }) => {
  return {

    '& .TaskCommentBody-header': {
      display: 'flex',
      alignItems: 'center',
      gap: theme.spacing(1),
      '& .MuiTypography-root': {
        fontWeight: 'bold'
      }
    },
    '& .TaskCommentBody-body': {
      borderTop: `1px solid ${theme.palette.divider}`,
      padding: theme.spacing(1),
      marginBottom: theme.spacing(1)
    },
    '& .TaskCommentBody-icon': {
      fontSize: '12pt',
      color: theme.palette.primary.main
    },
  }
})

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    header: ['header'],
    body: ['body'],
    icon: ['icon'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

