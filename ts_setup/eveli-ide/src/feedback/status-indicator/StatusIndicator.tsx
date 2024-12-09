import React from 'react';
import { Avatar, Tooltip, useTheme } from '@mui/material';
import DoneIcon from '@mui/icons-material/Done';
import DoNotDisturbAltIcon from '@mui/icons-material/DoNotDisturbAlt';
import { FeedbackApi, useFeedback } from '../../feedback';
import { useIntl } from 'react-intl';

export interface StatusIndicatorProps {
  size: 'SMALL' | 'LARGE';
  taskId: string | number;
}

export const StatusIndicator: React.FC<StatusIndicatorProps> = (props) => {
  const theme = useTheme();
  const intl = useIntl();
  const { findAllFeedback } = useFeedback();
  const [feedbacks, setFeedbacks] = React.useState<FeedbackApi.Feedback[]>();

  React.useEffect(() => {
    findAllFeedback()
      .then(resp => resp)
      .then((resp) => setFeedbacks(resp));
  }, [])

  const feedbackExists = feedbacks?.find(f => f.sourceId === props.taskId);

  if (!feedbackExists && props.size === 'SMALL') {
    return (
      <Tooltip title={intl.formatMessage({ id: 'feedback.notPublished' })}>
        <Avatar sx={{ height: '20px', width: '20px', backgroundColor: theme.palette.mainContent.contrastText }}>
          <DoNotDisturbAltIcon fontSize='inherit' />
        </Avatar>
      </Tooltip>
    )
  }

  if (!feedbackExists && props.size === 'LARGE') {
    return (
      <Tooltip title={intl.formatMessage({ id: 'feedback.notPublished' })}>
        <Avatar sx={{ height: '20pt', width: '20pt', backgroundColor: theme.palette.mainContent.contrastText }}>
          <DoNotDisturbAltIcon fontSize='small' />
        </Avatar>
      </Tooltip>)
  }

  if (props.size === 'SMALL') {
    return (
      <Tooltip title={intl.formatMessage({ id: 'feedback.isPublished' })}>
        <Avatar sx={{ height: 'auto', width: 'auto', backgroundColor: theme.palette.success.main }}>
          <DoneIcon fontSize='inherit' />
        </Avatar>
      </Tooltip>
    )
  }

  return (
    <Tooltip title={intl.formatMessage({ id: 'feedback.isPublished' })}>
      <Avatar sx={{ height: '20pt', width: '20pt', backgroundColor: theme.palette.success.main }}>
        <DoneIcon fontSize='small' />
      </Avatar>
    </Tooltip>
  )
}