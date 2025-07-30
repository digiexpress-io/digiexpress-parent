import React from 'react';
import { alpha, Box, Divider, generateUtilityClass, styled, Typography } from '@mui/material';
import { FeedbackApi, useFeedback } from '@/api-feedback';
import { TaskApi } from '@/api-task';
import composeClasses from '@mui/utils/composeClasses';
import { TaskCardStyleDefinition } from '../eveli-task-composer-v2-task-card';
import { PublishedNotifier } from './PublishedNotifier';

export const CustomerFeedbackReadOnly: React.FC<{ task: TaskApi.Task, style: TaskCardStyleDefinition }> = ({ task, style }) => {
  const { getOneFeedback } = useFeedback();
  const [feedback, setFeedback] = React.useState<FeedbackApi.Feedback>();
  const classes = useUtilityClasses();

  React.useEffect(() => {
    getOneFeedback(task.taskRef!)
      .then((resp) => {
        setFeedback(resp);
        console.log("Setting feedback:", resp);
      });
  }, [task.taskRef]);

  if (!feedback || !feedback.content) {
    return <Typography sx={{ ...style.bodyTypography }}>No feedback</Typography>;
  }


  return (
    <StyledCustomerFeedbackReadOnly className={classes.root}>
      <Box display='flex' flexDirection='row' justifyContent='space-between'>
        <Box flexDirection='column' alignItems='center'>
          <Typography className={classes.feedbackCategories} sx={{ ...style.bodyTypography }}>{feedback.content.main}</Typography>
          <Typography className={classes.feedbackCategories} sx={{ ...style.bodyTypography }}>{feedback.content.sub}</Typography>
        </Box>
        <Box alignItems='top'>
          <PublishedNotifier task={task} style={style} />
        </Box>
      </Box>
      <Divider sx={{ my: 1 }} />
      <Typography sx={{ ...style.bodyTypography }}>Feedback title: {feedback.content.title}</Typography>
      <Typography sx={{ ...style.bodyTypography }}>Detailed response: {feedback.content.question}</Typography>
    </StyledCustomerFeedbackReadOnly>
  )
}


const MUI_NAME = 'CustomerFeedbackReadOnly';
const StyledCustomerFeedbackReadOnly = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',

  '& .CustomerFeedbackReadOnly-publishedNotifier': {
    alignSelf: 'flex-end',
    display: 'flex',
    alignItems: 'center',
    backgroundColor: alpha(theme.palette.error.main, 0.1),
    padding: theme.spacing(0.5),
    borderRadius: theme.spacing(1),
    border: `1px solid ${theme.palette.error.main}`,
    color: theme.palette.error.main,

    '.MuiSvgIcon-root': {
      fontSize: 'small',
      color: 'red'
    },

  },
  '& .CustomerFeedbackReadOnly-feedbackCategories': {
    fontWeight: 500,
  },


}));

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    publishedNotifier: ['publishedNotifier'],
    feedbackCategories: ['feedbackCategories']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};