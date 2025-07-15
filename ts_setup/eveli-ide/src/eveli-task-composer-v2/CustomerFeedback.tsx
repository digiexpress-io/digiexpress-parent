import React from 'react';
import { alpha, Box, Chip, Divider, generateUtilityClass, styled, Typography } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { FeedbackApi, useFeedback } from '@/api-feedback';
import { TaskApi } from '@/api-task';
import composeClasses from '@mui/utils/composeClasses';
import { TaskCardStyleDefinition } from './cardThemeConfig';

export const CustomerFeedback: React.FC<{ task: TaskApi.Task, style: TaskCardStyleDefinition }> = ({ task, style }) => {
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
    <StyledCustomerFeedback className={classes.root}>
      <Box className={classes.publishedNotifier} sx={{ ...style.bodyTypographySmall }}><CloseIcon />Not published</Box>
      <Typography className={classes.feedbackCategories} sx={{ ...style.bodyTypography }}>{feedback.content.main}</Typography>
      <Typography className={classes.feedbackCategories} sx={{ ...style.bodyTypography }}>{feedback.content.sub}</Typography>
      <Divider sx={{ my: 1 }} />
      <Typography sx={{ ...style.bodyTypography }}>Customer wrote: {feedback.content.title}</Typography> 
      <Typography>{feedback.content.question}</Typography>
    </StyledCustomerFeedback>
  )
}


const MUI_NAME = 'CustomerFeedback';
const StyledCustomerFeedback = styled('div', {
  name: MUI_NAME,
  slot: 'Message',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',

  '& .CustomerFeedback-publishedNotifier': {
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
  '& .CustomerFeedback-feedbackCategories': {
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