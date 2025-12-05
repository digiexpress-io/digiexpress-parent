import React from 'react';

import { Box, FormControl, MenuItem, Select, TextField, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FeedbackApi, useFeedback } from '../api-feedback';



export const FeedbackContent: React.FC<{
  feedback: FeedbackApi.FeedbackContent
  onChange: (props: {
    labelKey: string;
    labelValue: string;
    customerTitle: string;
  }) => void
}> = ({ feedback, onChange }) => {
  const intl = useIntl();

  const handleMainChange = (value: FeedbackApi.FeedbackTopic) => {
    onChange({
      labelKey: value.labelKey,
      labelValue: value.labelValue,
      customerTitle: feedback.customerTitle ?? ''
    })
  }

  const handleCustomerTitleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    onChange({
      labelKey: feedback.labelKey,
      labelValue: feedback.labelValue,
      customerTitle: event.currentTarget.value ?? ''
    })
  }

  if (!feedback) {
    return;
  }
  return (<>
    <div style={{ marginBottom: 10 }}>
      <FeedbackTopicSelect onChange={handleMainChange} value={feedback.labelKey} />
    </div>
    <div style={{ marginBottom: 10 }}>
      <Typography fontWeight='bold'>{intl.formatMessage({ id: 'feedback.customerTitle' })}</Typography>
      <TextField sx={{ width: '100%' }} value={feedback.customerTitle ?? ''} onChange={handleCustomerTitleChange} />
    </div>
  </>)
}



const FeedbackTopicSelect: React.FC<{
  onChange: (value: FeedbackApi.FeedbackTopic) => void,
  value: string;
}> = ({ onChange, value }) => {

  const { getFeedbackTopics } = useFeedback();
  const values = getFeedbackTopics(value);
  const found = values.find(item => item.selected)

  return (
    <Box sx={{ minWidth: 120 }}>
      <FormControl fullWidth>
        <Select value={found?.labelKey ?? ''}>
          {values.map(topic => (
            <MenuItem key={topic.labelKey} value={topic.labelKey} onClick={() => onChange(topic)}>
              {topic.labelValue}
            </MenuItem>
          ))
          }
        </Select>
      </FormControl>
    </Box>
  );
}