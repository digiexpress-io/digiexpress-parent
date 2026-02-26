import React from 'react';
import { Box, Typography } from '@mui/material';
import Editor from '@monaco-editor/react';
import YAML from 'yaml';

import { useTaskDashboard } from '../task-dashboard';
import { FeedbackApi, useFeedback } from '@dxs-ts/task-feedback';
import { useIntl } from 'react-intl';

const toYaml = (props: any) => {
  const doc = new YAML.Document();
  doc.contents = props;
  return doc.toString();
}

export const TaskAuditAi: React.FC = () => {
  const intl = useIntl();
  const { task } = useTaskDashboard();
  const { getFeedbackSentimentAndSubcategory, getSimilarFeedback } = useFeedback();
  const [sentimentAndSubcategory, setSentimentAndSubcategory] = React.useState<FeedbackApi.SentimentAndSubcategory | undefined>(undefined);
  const [similarFeedback, setSimilarFeedback] = React.useState<FeedbackApi.SimilarFeedback | undefined>(undefined);

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

  const displaySentimentAndSubcategory = sentimentAndSubcategory ? sentimentAndSubcategory : { message: intl.formatMessage({ id: 'task.audit.ai.sentiment.unavailable' }) };
  const displaySimilar = similarFeedback ? similarFeedback : { message: intl.formatMessage({ id: 'task.audit.ai.similar.unavailable' }) };

  return (
    <Box>
      <Typography variant='h4' sx={{ mb: 2 }}>{intl.formatMessage({ id: 'task.audit.ai.sentiment.and.subcategory' })}</Typography>
        <Editor
          value={toYaml(displaySentimentAndSubcategory)}
          onChange={() => {}}
          defaultLanguage='yaml'
          height='200px'
        />
      <Typography variant='h4' sx={{ my: 2 }}>{intl.formatMessage({ id: 'task.audit.ai.similar.feedback' })}</Typography>
        <Editor
          value={toYaml(displaySimilar)}
          onChange={() => {}}
          defaultLanguage='yaml'
          height='200px'
        />
    </Box>
  );
}