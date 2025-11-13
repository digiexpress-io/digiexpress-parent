import React from 'react';
import { Box, Divider, Typography } from '@mui/material';

import { useTaskDashboard } from '../task-dashboard';
import { FeedbackApi, useFeedback } from '@dxs-ts/task-feedback';
import { useIntl } from 'react-intl';
import { TaskCardDataRowElement, useCardConfig, useTaskCardThemeConfig } from '../task-card';
import { TaskFeedbackSentiment } from '../task-feedback-sentiment';
import { TaskFeedbackSubcategory } from '../task-feedback-subcategory';
import { Stack } from '@mui/system';
import { TaskFeedbackSimilar } from '../task-feedback-similar';


export const TaskAiAssistant: React.FC = () => {
  const intl = useIntl();
  const { task } = useTaskDashboard();
  const { getFeedbackSentimentAndSubcategory, getSimilarFeedback } = useFeedback();

  const { cardTheme } = useCardConfig();
  const styleConfig = useTaskCardThemeConfig();
  const style = styleConfig[cardTheme];

  const [sentimentAndSubcategory, setSentimentAndSubcategory] = React.useState<FeedbackApi.SentimentAndSubcategoryResponse | undefined>(undefined);
  const [similarFeedback, setSimilarFeedback] = React.useState<FeedbackApi.SimilarityResponse | undefined>(undefined);
  const [showSimilarFeedback, setShowSimilarFeedback] = React.useState<boolean>(false);
  const similarities = similarFeedback?.entries.find((entry) => entry.id === task.id)?.similarities;

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

  if (!sentimentAndSubcategory && !similarFeedback) {
    return <Typography sx={{ ...style.bodyTypography }}>{intl.formatMessage({ id: 'task.ai.assistant.loading', defaultMessage: 'Loading AI Assistant data...' })}</Typography>;
  }

  return (
    <Stack direction="column" height="100%">
      <TaskCardDataRowElement label={intl.formatMessage({ id: 'taskcard.body.sentiment', defaultMessage: 'Sentiment' })} style={style} 
        value={<TaskFeedbackSentiment sentiment={sentimentAndSubcategory?.sentiment.sentiment} style={style} />} />
      <Divider sx={{ mb: 1 }} />
      <TaskCardDataRowElement label={intl.formatMessage({ id: 'taskcard.body.subcategory', defaultMessage: 'Subcategory' })} style={style} 
        value={<TaskFeedbackSubcategory subcategory={sentimentAndSubcategory?.subcategory.subcategory} style={style} />} />
      <Divider sx={{ mb: 1 }} />
      <TaskCardDataRowElement label={intl.formatMessage({ id: 'taskcard.body.similar.feedback', defaultMessage: 'Similar feedback' })} style={style} 
        value={<TaskFeedbackSimilar similarities={similarities} style={style} toggleShowSimilarities={() => setShowSimilarFeedback(!showSimilarFeedback)} />} />
      {showSimilarFeedback && similarities && similarities.length > 0 && <Box sx={{ p: 0.5, mt: 1, border: '1px solid', borderColor: 'divider', borderRadius: 1, maxHeight: 200, overflowY: 'auto' }}>
        {similarities.map((similarity, index) => (
          <>
            <Box key={similarity.id} sx={{ mb: 1 }} onClick={() => {
              window.open(`/secured/en/worker/feedback/${similarity.id}`, '_blank');
            }}>
              <Typography>{similarity.text}</Typography>
            </Box>
            {index < ((similarities?.length ?? 0) - 1) && <Divider sx={{ mb: 1 }} />}
          </>
        ))}
      </Box>}
    </Stack>
  );
}