import React from 'react';
import { Avatar, Tooltip, useTheme } from '@mui/material';
import { Done as DoneIcon } from '@mui/icons-material';
import { RateReview as RateReviewIcon } from '@mui/icons-material';

import { FeedbackApi, useFeedback } from '../api-feedback';
import { useIntl } from 'react-intl';

export interface StatusIndicatorProps {
  size: 'SMALL' | 'LARGE';
  taskId: string | number;
}

export const StatusIndicator: React.FC<StatusIndicatorProps> = (props) => {
  const theme = useTheme();
  const intl = useIntl();
  const { getOneFeedback } = useFeedback();
  const [feedbacks, setFeedbacks] = React.useState<FeedbackApi.Feedback>();

  React.useEffect(() => {
    getOneFeedback(props.taskId + '')
      .then(resp => resp)
      .then((resp) => setFeedbacks(resp));
  }, [])

  const feedbackExists = feedbacks ? true : false;

  if (!feedbackExists && props.size === 'SMALL') {
    return (
      <Tooltip title={intl.formatMessage({ id: 'feedback.notPublished' })}>
        <Avatar sx={{ height: '20pt', width: '20pt', backgroundColor: theme.palette.error.main }}>
          <RateReviewIcon fontSize='inherit' />
        </Avatar>
      </Tooltip>
    )
  }

  if (!feedbackExists && props.size === 'LARGE') {
    return (
      <Tooltip title={intl.formatMessage({ id: 'feedback.notPublished' })}>
        <Avatar sx={{ height: '20pt', width: '20pt', backgroundColor: theme.palette.error.main }}>
          <RateReviewIcon fontSize='small' />
        </Avatar>
      </Tooltip>)
  }

  if (props.size === 'SMALL') {
    return (
      <Tooltip title={intl.formatMessage({ id: 'feedback.isPublished' })}>
        <Avatar sx={{ height: '20pt', width: '20pt', backgroundColor: theme.palette.success.main }}>
          <RateReviewIcon fontSize='inherit' />
        </Avatar>
      </Tooltip>
    )
  }

  return (
    <Tooltip title={intl.formatMessage({ id: 'feedback.isPublished' })}>
      <Avatar sx={{ height: '20pt', width: '20pt', backgroundColor: theme.palette.success.main }}>
        <RateReviewIcon fontSize='small' />
      </Avatar>
    </Tooltip>
  )
}