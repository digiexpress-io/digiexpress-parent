import React from 'react';

import { Box, FormControl, InputLabel, MenuItem, Select, TextField, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FeedbackApi, useFeedback } from '../api-feedback';



export const FeedbackContent: React.FC<{
  feedback: FeedbackApi.FeedbackContent
  onChange: (props: {
    labelKey: string;
    subLabelKey: string | undefined;
    labelValue: string;
    subLabelValue: string | undefined;
    customerTitle: string;
  }) => void
}> = ({ feedback, onChange }) => {

  const intl = useIntl();
  const initData = React.useMemo(() => feedback, []);
  const [feedbackTopics, setFeedbackTopics] = React.useState<FeedbackApi.FeedbackTopic>();
  const { getFeedbackTopics } = useFeedback();

  React.useEffect(() => {
    getFeedbackTopics(initData).then(setFeedbackTopics)
  }, [feedback, getFeedbackTopics]);


  const handleMainChange = (value: FeedbackApi.FeedbackTopicItem) => {
    onChange({
      labelKey: value.labelKey,
      labelValue: value.labelValue,
      subLabelKey: feedback.subLabelKey,
      subLabelValue: feedback.subLabelValue,
      customerTitle: feedback.customerTitle ?? ''
    })
  }

  const handleCustomerTitleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    onChange({
      labelKey: feedback.labelKey,
      labelValue: feedback.labelValue,
      subLabelKey: feedback.subLabelKey,
      subLabelValue: feedback.subLabelValue,
      customerTitle: event.currentTarget.value ?? ''
    })
  }

  if (!feedback) {
    return;
  }
  return (<>
    <div style={{ marginTop: 25 }} />

    <div style={{ marginBottom: 10 }}>
      <FeedbackTopicSelect onChange={handleMainChange}
        label={intl.formatMessage({ id: 'feedback.mainCategory' })}
        value={feedback.labelKey}
        values={feedbackTopics?.main ?? []}
      />
    </div>

    <div style={{ marginBottom: 10 }}>
      <Typography fontWeight='bold'>{intl.formatMessage({ id: 'feedback.customerTitle' })}</Typography>
      <TextField sx={{ width: '100%' }} value={feedback.customerTitle ?? ''} onChange={handleCustomerTitleChange} />
    </div>

    </>)
  }



const FeedbackTopicSelect: React.FC<{
  onChange: (value: FeedbackApi.FeedbackTopicItem) => void,
  label: string;
  values: FeedbackApi.FeedbackTopicItem[],
  value: string;
}> = ({ onChange, value: initValue, values, label }) => {

  const value = initValue.toLocaleLowerCase();
  const invalid = !values.find(topic => topic.labelKey === value);

  if (invalid) {
    //console.error("CANT FIND VALUE", value);
  }

  return (
    <Box sx={{ minWidth: 120 }}>
      <FormControl fullWidth>
        <InputLabel>
          <Typography fontWeight='bold'>{label}</Typography>
        </InputLabel>
        <Select value={value} label={label}>
          {invalid && (
            <MenuItem key={value} value={value} onClick={() => { }}>
              * not translated {value}
            </MenuItem>)
          }
          {values.map(topic => (
            <MenuItem key={topic.labelKey} value={topic.labelKey} onClick={() => onChange(topic)}>
              {topic.labelValue}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
    </Box>
  );
}