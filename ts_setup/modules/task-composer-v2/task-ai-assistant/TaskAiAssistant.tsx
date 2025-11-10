import React from 'react';
import { Box } from '@mui/material';

import { useTaskDashboard } from '../task-dashboard';
import { FeedbackApi, useFeedback } from '@dxs-ts/task-feedback';
import { useIntl } from 'react-intl';
import { TaskCardDataRowElement, useCardConfig, useTaskCardThemeConfig } from '../task-card';
import { TaskFeedbackSentiment } from '../task-feedback-sentiment';


export const TaskAiAssistant: React.FC = () => {
  const intl = useIntl();
  const { task } = useTaskDashboard();
  const { getFeedbackSentimentAndSubcategory, getSimilarFeedback } = useFeedback();

  const { cardTheme } = useCardConfig();
  const styleConfig = useTaskCardThemeConfig();
  const style = styleConfig[cardTheme];

  const [sentimentAndSubcategory, setSentimentAndSubcategory] = React.useState<FeedbackApi.SentimentAndSubcategoryResponse | undefined>(undefined);
  const [similarFeedback, setSimilarFeedback] = React.useState<FeedbackApi.SimilarityResponse | undefined>(undefined);

  React.useEffect(() => {
    getFeedbackSentimentAndSubcategory(task.id).then(result => {
      setSentimentAndSubcategory(result);
    });
  }, [task.id, getFeedbackSentimentAndSubcategory]);

  React.useEffect(() => {
    getSimilarFeedback(task.id).then(result => {
      setSimilarFeedback(result);
    });
  }, [task.id, getSimilarFeedback]);

  return (
    <Box>
      <TaskCardDataRowElement label={intl.formatMessage({ id: 'taskcard.body.sentiment', defaultMessage: 'Sentiment' })} style={style} value={<TaskFeedbackSentiment sentiment={sentimentAndSubcategory?.sentiment.sentiment} style={style} />} />
    </Box>
  );
}