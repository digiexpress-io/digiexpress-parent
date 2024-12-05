import React from 'react';
import { Avatar, useTheme } from '@mui/material';
import DoneIcon from '@mui/icons-material/Done';
import DoNotDisturbAltIcon from '@mui/icons-material/DoNotDisturbAlt';
import { FeedbackApi, useFeedback } from '../../feedback';

export interface StatusIndicatorProps {
  size: 'SMALL' | 'LARGE';
  taskId: string | number;
}

export const StatusIndicator: React.FC<StatusIndicatorProps> = (props) => {
  const theme = useTheme();
  const { findAllFeedback } = useFeedback();
  const [feedbacks, setFeedbacks] = React.useState<FeedbackApi.Feedback[]>();

  React.useEffect(() => {
    findAllFeedback()
      .then(resp => resp)
      .then((resp) => setFeedbacks(resp));
  }, [])

  const feedbackExists = feedbacks?.find(f => f.id === props.taskId);

  if (!feedbackExists && props.size === 'SMALL') {
    return (
      <Avatar sx={{ height: '20px', width: '20px', backgroundColor: theme.palette.mainContent.contrastText }}>
        <DoNotDisturbAltIcon fontSize='inherit' />
      </Avatar>)
  }

  if (!feedbackExists && props.size === 'LARGE') {
    return (
      <Avatar sx={{ height: '20pt', width: '20pt', backgroundColor: theme.palette.mainContent.contrastText }}>
        <DoNotDisturbAltIcon fontSize='small' />
      </Avatar>)
  }

  if (props.size === 'SMALL') {
    return (
      <Avatar sx={{ height: 'auto', width: 'auto', backgroundColor: theme.palette.success.main }}>
        <DoneIcon fontSize='inherit' />
      </Avatar>
    )
  }

  return (
    <Avatar sx={{ height: '20pt', width: '20pt', backgroundColor: theme.palette.success.main }}>
      <DoneIcon fontSize='small' />
    </Avatar>)
}