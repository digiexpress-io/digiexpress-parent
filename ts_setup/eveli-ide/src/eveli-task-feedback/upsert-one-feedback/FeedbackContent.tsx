import React from 'react';

import { Box, FormControl, InputLabel, MenuItem, Select, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FeedbackApi, useFeedback } from '@/api-feedback';


  /*
  Mistä aiheesta haluat antaa palautetta? = main
  Valitse aihe = sub
  Otsikko = title
  Kuvaus = question
  */


export const FeedbackContent: React.FC<{
  feedback: FeedbackApi.FeedbackContent
  onChange: (props: {
    labelKey: string;
    subLabelKey: string | undefined;
    labelValue: string;
    subLabelValue: string | undefined;
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
      subLabelValue: feedback.subLabelValue
    })
  }


  const handleSubChange = (value: FeedbackApi.FeedbackTopicItem) => {
    onChange({
      labelKey: feedback.labelKey,
      labelValue: feedback.labelValue,
      subLabelKey: value.labelKey,
      subLabelValue: value.labelValue
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
      <FeedbackTopicSelect onChange={handleSubChange}
        label={intl.formatMessage({ id: 'feedback.subCategory' })}
        value={feedback.subLabelKey ?? ''}
        values={feedbackTopics?.sub ?? []}
      />
    </div>

    {feedback.content.title &&
      <div style={{ marginBottom: 10 }}>
        <Typography fontWeight='bold'>{intl.formatMessage({ id: 'feedback.customerTitle' })}</Typography>
        <Typography>{feedback.content.title}</Typography>
      </div>
    }
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