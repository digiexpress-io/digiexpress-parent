import React from 'react';
import { alpha, Box, generateUtilityClass, styled, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import composeClasses from '@mui/utils/composeClasses';

import { FeedbackApi, useFeedback } from '@dxs-ts/task-feedback';
import { TaskApi } from '@dxs-ts/task-api';


import { TaskCardStyleDefinition } from '../task-card';
import { PublishedNotifier } from './PublishedNotifier';



export const CustomerFeedbackReadOnly: React.FC<{ task: TaskApi.Task, style: TaskCardStyleDefinition }> = ({ task, style }) => {
  const intl = useIntl();
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
    return (
      <Typography color='error' sx={{ ...style.bodyTypography }}>
        {intl.formatMessage({ id: 'task.feedback.none' })}
      </Typography>)
  }

  return (
    <StyledCustomerFeedbackReadOnly className={classes.root}>
      <Box display='flex' flexDirection='row' justifyContent='space-between'>
        <Box flexDirection='column' alignItems='center'>
          <Typography className={classes.feedbackCategories} sx={{ ...style.bodyTypography }}>{feedback.content.main}</Typography>
        </Box>
        <Box alignItems='top'>
          <PublishedNotifier task={task} style={style} />
        </Box>
      </Box>

      <Box className={classes.leftBorder}>

        <Box className={classes.customerText}>
          <Typography sx={{ ...style.bodyTypography, fontWeight: 'bold' }}>
            {intl.formatMessage({ id: 'task.feedback.title' })}
        </Typography>
        <Typography sx={{ ...style.bodyTypography }}>{feedback.content.title}</Typography>
      </Box>


      <Box className={classes.customerText}>
          <Typography sx={{ ...style.bodyTypography, fontWeight: 'bold' }}>
            {intl.formatMessage({ id: 'task.feedback.detailedResponse' })}
        </Typography>
          <Typography sx={{ ...style.bodyTypography }}>{truncate(feedback.content.question, 350)}</Typography>
        </Box>
      </Box>
    </StyledCustomerFeedbackReadOnly>
  )
}

function truncate(text: string | undefined, maxLength: number) {
  if (!text) {
    return;
  }
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text;
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
  gap: theme.spacing(1),

  '& .MuiDivider-root': {
    marginTop: theme.spacing(1),
    marginBottom: theme.spacing(1)
  },
  '& .CustomerFeedbackReadOnly-customerText': {
    padding: theme.spacing(0.5),
  },

  '& .CustomerFeedbackReadOnly-leftBorder': {
    borderLeft: `3px solid ${theme.palette.primary.main}`,
    paddingLeft: theme.spacing(1),
    backgroundColor: alpha(theme.palette.primary.main, 0.06)
  },

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
    feedbackCategories: ['feedbackCategories'],
    customerText: ['customerText'],
    leftBorder: ['leftBorder']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};