import React from 'react';

import { Box, FormControl, MenuItem, Select, TextField, Typography } from '@mui/material';
import { useUtilityClasses } from './useUtilityClasses';
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
  const classes = useUtilityClasses();
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
    <div className={classes.section}>
      <FeedbackTopicSelect onChange={handleMainChange}
        label={intl.formatMessage({ id: 'feedback.mainCategory' })}
        value={feedback.labelKey}
        values={feedbackTopics?.main ?? []}
      />
    </div>

    <div className={classes.section}>
      <Typography className={classes.boldLabel}>
        {intl.formatMessage({ id: 'feedback.customerTitle' })}
      </Typography>
      <TextField
        className={classes.field}
        fullWidth
        value={feedback.customerTitle ?? ''}
        onChange={handleCustomerTitleChange}
      />
    </div>

  </>)
}



const FeedbackTopicSelect: React.FC<{
  onChange: (value: FeedbackApi.FeedbackTopicItem) => void,
  label: string;
  value: string;
  values: FeedbackApi.FeedbackTopicItem[],
}> = ({ onChange, value, values, label }) => {
  const classes = useUtilityClasses();

  const found = values.find(topic => topic.labelKey.toLocaleLowerCase().endsWith(`.${value.toLocaleLowerCase()}`))?.labelKey;
  if(!found) {
    console.error('Feedback topic not found', { value, values })
  }


  return (
    <Box sx={{ minWidth: 120 }}>
      <Typography className={classes.boldLabel}>
        {label}
      </Typography>
      <FormControl fullWidth>
        <Select value={found ?? value} displayEmpty>
          {values
            .filter(({ labelKey }) => {
              const isFailsafe = labelKey === value;
              if (found && isFailsafe) return false;
                  return true;
                })
                .map(topic => (
                  <MenuItem
                    key={topic.labelKey}
                    value={topic.labelKey}
                    onClick={() => onChange(topic)}
                  >
                    {topic.labelValue}
                  </MenuItem>
                ))}
            </Select>
          </FormControl>
        </Box>
      );
}