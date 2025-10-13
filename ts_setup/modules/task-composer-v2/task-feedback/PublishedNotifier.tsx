import React from 'react';
import { alpha, generateUtilityClass, styled } from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { Check as CheckIcon } from '@mui/icons-material';
import composeClasses from '@mui/utils/composeClasses';

import { useIntl } from 'react-intl';
import { FeedbackApi, useFeedback } from '@dxs-ts/task-feedback';

import { TaskApi } from '@dxs-ts/task-api';
import { TaskCardStyleDefinition } from '../task-card';


export interface PublishedNotifierProps {
  task: TaskApi.Task;
  style?: TaskCardStyleDefinition;
}


export const PublishedNotifier: React.FC<PublishedNotifierProps> = ({ task, style }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  const { getOneFeedback } = useFeedback();
  const [feedbacks, setFeedbacks] = React.useState<FeedbackApi.Feedback>();


  React.useEffect(() => {
    getOneFeedback(task.taskRef!)
      .then(resp => resp)
      .then((resp) => setFeedbacks(resp));
  }, [])

  const feedbackExists = feedbacks ? true : false;


  if (feedbackExists) {
    return (
      <NotifierPublished className={classes.msgContainer} sx={{ ...style?.bodyTypographySmall }}>
        <CheckIcon />
        {intl.formatMessage({ id: 'task.feedback.isPublished', defaultMessage: 'Published' })}
      </NotifierPublished>)

  }

  return (
    <NotifierUnpublished className={classes.msgContainer} sx={{ ...style?.bodyTypographySmall }}>
      <CloseIcon />
      {intl.formatMessage({ id: 'task.feedback.isNotPublished', defaultMessage: 'Not published' })}
    </NotifierUnpublished>
  )
}


const MUI_NAME = 'FeedbackPublishedNotifier';
const NotifierUnpublished = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  backgroundColor: alpha(theme.palette.error.main, 0.1),
  padding: theme.spacing(0.5),
  borderRadius: theme.spacing(1),
  border: `1px solid ${theme.palette.error.main}`,
  color: theme.palette.error.main,
  fontSize: theme.typography.body2.fontSize,

  '.MuiSvgIcon-root': {
    fontSize: 'small',
    color: 'red',
    marginRight: theme.spacing(1)
  },

}));

const NotifierPublished = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  backgroundColor: alpha(theme.palette.success.main, 0.1),
  padding: theme.spacing(0.5),
  borderRadius: theme.spacing(1),
  border: `1px solid ${theme.palette.success.main}`,
  color: theme.palette.success.main,
  fontSize: theme.typography.body2.fontSize,

  '.MuiSvgIcon-root': {
    fontSize: 'small',
    color: theme.palette.success.main,
    marginRight: theme.spacing(1)
  },

}));

export const useUtilityClasses = () => {
  const slots = {
    msgContainer: ['msgContainer'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};
