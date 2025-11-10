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

    const displaySentimentAndSubcategory = sentimentAndSubcategory ? sentimentAndSubcategory : { message: "No AI analysis for sentiment and subcategory available" };
    const displaySimilar = similarFeedback ? similarFeedback : { message: "No similar feedback available" };

    return (
        <Box>
            <Typography variant='h4' sx={{ mb: 2 }}>{intl.formatMessage({ id: 'task.audit.ai.sentiment.and.subcategory', defaultMessage: "Sentiment and Subcategory" })}</Typography>
            <Editor
                value={toYaml(displaySentimentAndSubcategory)}
                onChange={() => {}}
                defaultLanguage='yaml'
                height='200px'
            />
            <Typography variant='h4' sx={{ my: 2 }}>{intl.formatMessage({ id: 'task.audit.ai.similar.feedback', defaultMessage: "Similar Feedback" })}</Typography>
            <Editor
                value={toYaml(displaySimilar)}
                onChange={() => {}}
                defaultLanguage='yaml'
                height='200px'
            />
        </Box>
    );
}