import React from 'react';
import { alpha, Box, Divider, generateUtilityClass, styled, Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import { FeedbackApi, useFeedback } from '@dxs-ts/eveli-api';
import { TaskApi } from '@dxs-ts/eveli-api';
import composeClasses from '@mui/utils/composeClasses';
import { TaskCardStyleDefinition } from '../eveli-task-composer-v2-task-card';
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
        {intl.formatMessage({ id: 'task.feedback.none', defaultMessage: 'No feedback found' })}
      </Typography>)
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

      <Box className={classes.customerTitle}>
        <Typography sx={{ ...style.bodyTypography, fontWeight: 'bold', marginBottom: 1 }}>
          {intl.formatMessage({ id: 'task.feedback.title', defaultMessage: 'Customer title' })}
        </Typography>
        <Typography sx={{ ...style.bodyTypography }}>{feedback.content.title}</Typography>
      </Box>

      <Box className={classes.customerText}>
        <Typography sx={{ ...style.bodyTypography, fontWeight: 'bold', marginBottom: 1 }}>
          {intl.formatMessage({ id: 'task.feedback.detailedResponse', defaultMessage: 'Details from customer' })}
        </Typography>
        <Typography sx={{ ...style.bodyTypography }}>{truncate(feedback.content.question, 150)}</Typography>
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
  '& .CustomerFeedbackReadOnly-customerTitle': {
    backgroundColor: alpha(theme.palette.primary.dark, 0.1),
    border: `1px solid ${alpha(theme.palette.primary.dark, 0.15)}`,
    padding: theme.spacing(1),
    borderRadius: theme.spacing(2),
  },


  '& .CustomerFeedbackReadOnly-customerText': {
    backgroundColor: alpha(theme.palette.primary.main, 0.1),
    border: `1px solid ${alpha(theme.palette.primary.main, 0.15)}`,
    padding: theme.spacing(1),
    borderRadius: theme.spacing(2)
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
    customerTitle: ['customerTitle']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};