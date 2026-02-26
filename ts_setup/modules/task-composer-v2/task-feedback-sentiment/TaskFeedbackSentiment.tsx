import React from 'react';
import { Chip, generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';
import { TaskCardStyleDefinition } from '../task-card';
import { FeedbackApi, FeedbackBackend } from '@dxs-ts/task-feedback';
import { TaskApi } from '@dxs-ts/task-api';

export interface TaskFeedbackSentimentProps {
  sentiment: FeedbackApi.SentimentPolarity | undefined;
  style: TaskCardStyleDefinition
}

const getSentimentColor = (sentiment: FeedbackApi.SentimentPolarity | undefined): string => {
  if (!sentiment) {
    return '#ccc';
  }

  const colorEnum = FeedbackApi.sentiment_colors[sentiment.toUpperCase()];

  switch (colorEnum) {
    case FeedbackApi.Colors.RED:
      return '#f44336';
    case FeedbackApi.Colors.GREEN:
      return '#4caf50';
    case FeedbackApi.Colors.YELLOW:
      return '#ffeb3b';
    case FeedbackApi.Colors.GREY:
      return '#9e9e9e';
    default:
      return '#ccc';
  }
};

const getContrastText = (hex: string): string => {
  const c = hex.substring(1);
  const rgb = parseInt(c, 16);
  const r = (rgb >> 16) & 0xff;
  const g = (rgb >> 8) & 0xff;
  const b = rgb & 0xff;

  const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
  return luminance > 0.6 ? '#000000' : '#ffffff';
};

export const TaskFeedbackSentiment: React.FC<TaskFeedbackSentimentProps> = ({ sentiment, style }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();

  if (!sentiment) {
    return (
      <TaskFeedbackSentimentRoot sentiment={sentiment}>
        <Typography color='error' sx={{ ...style.bodyTypography }}>{intl.formatMessage({ id: 'task.sentiment.none' })}</Typography>
      </TaskFeedbackSentimentRoot>)
  }

  const label = sentiment.charAt(0).toUpperCase() + sentiment.slice(1).toLowerCase();

  return (
    <TaskFeedbackSentimentRoot sentiment={sentiment} className={classes.root}>
      <Chip label={label} variant='filled' />
    </TaskFeedbackSentimentRoot>
  )
}


const MUI_NAME = 'TaskFeedbackSentiment';
const TaskFeedbackSentimentRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})<{ sentiment: FeedbackApi.SentimentPolarity | undefined }>(({ sentiment, theme }) => {

  const bgColor = getSentimentColor(sentiment);
  const textColor = getContrastText(bgColor);

  return {
    display: 'flex',
    alignItems: 'center',
    flexWrap: 'wrap',
    marginBottom: theme.spacing(1),
    gap: theme.spacing(1),

    '& .MuiChip-root': {
      backgroundColor: bgColor,
      color: textColor,
    }
  };
})


export const useUtilityClasses = () => {
  const slots = {
    root: ['root']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
