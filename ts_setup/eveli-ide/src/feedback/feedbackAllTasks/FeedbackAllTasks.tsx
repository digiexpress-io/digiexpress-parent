import React from 'react';
import { Box, Divider, FormControl, List, ListItem, ListItemButton, MenuItem, Select, SelectChangeEvent, TextField, Typography, useTheme } from '@mui/material';
import { useIntl } from 'react-intl';
import { useNavigate } from 'react-router-dom';

import * as Burger from '@/burger';
import { FeedbackApi, useFeedback } from '../feedback-api';
import { StatusIndicator } from '../status-indicator';



// need two filters -- one for category and one for sub-category or type

const categories = [
  'Public pools',
  'Parks and recreation',
  'School services',
  'Community events',
];

const subCategories = [
  'Cleanliness and hygine',
  'Changing facilities',
  'Customer service'
]


export interface FeedbackAllTasksProps {
}

export const FeedbackAllTasks: React.FC<FeedbackAllTasksProps> = () => {
  const intl = useIntl();
  const navigate = useNavigate();
  const theme = useTheme();

  const { findAllFeedback } = useFeedback();
  const [feedback, setFeedback] = React.useState<FeedbackApi.Feedback[]>();

  React.useEffect(() => {
    findAllFeedback()
      .then(data => setFeedback(data));
  }, []);


  const [category, setCategory] = React.useState<string[]>([]);
  const [subCategory, setSubCategory] = React.useState<string[]>([]);

  const handleChangeCategory = (event: SelectChangeEvent<typeof category>) => {
    const {
      target: { value },
    } = event;
    setCategory(
      // On autofill we get a stringified value.
      typeof value === 'string' ? value.split(',') : value,
    );
  };

  const handleChangeSubCategory = (event: SelectChangeEvent<typeof category>) => {
    const {
      target: { value },
    } = event;
    setSubCategory(
      // On autofill we get a stringified value.
      typeof value === 'string' ? value.split(',') : value,
    );
  };

  function handleFeedbackNav(taskId: string) {
    console.log(taskId)
    navigate(`/feedback/${taskId}`);

  }

  return (
    <Box padding={theme.spacing(3)}>
      <Typography variant='h1'>{intl.formatMessage({ id: 'feedback.all' })}</Typography>

      <Box display='flex' mb={3} gap={1} alignItems='end'>
        <Box width='25%'>
          <Burger.TextField label='feedback.search' onChange={() => { }} value={intl.formatMessage({ id: 'feedback.search.placeholder' })} />
        </Box>

        <Box width='75%'>
          <FormControl sx={{ width: '45%', marginRight: 1 }}>
            <Typography fontWeight='bold'>{intl.formatMessage({ id: 'feedback.search.filter.category' })}</Typography>
            <Select
              sx={{ padding: 0 }}
              value={category}
              onChange={handleChangeCategory}
              input={<TextField select sx={{ padding: 0 }} />}
              renderValue={(selected) => (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                  {selected.map((value) => (<>{value}</>))}
                </Box>
              )}
            >
              {categories.map((name) => (
                <MenuItem
                  key={name}
                  value={name}
                >
                  {name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <FormControl sx={{ width: '45%' }}>
            <Typography fontWeight='bold'>{intl.formatMessage({ id: 'feedback.search.filter.subCategory' })}</Typography>
            <Select
              sx={{ padding: 0 }}
              value={subCategory}
              onChange={handleChangeSubCategory}
              input={<TextField select sx={{ padding: 0 }} />}
              renderValue={(selected) => (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                  {selected.map((value) => (<>{value}</>))}
                </Box>
              )}
            >
              {subCategories.map((name) => (
                <MenuItem
                  key={name}
                  value={name}
                >
                  {name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

        </Box>
      </Box>

      <List dense disablePadding>

        {feedback ? feedback.map((feedback) => (<>
          <ListItem dense disableGutters>
            <ListItemButton onClick={() => handleFeedbackNav(feedback.sourceId)}>
              <Box display='flex' gap={3} width='100%'>
                <Box width='6%' alignContent='center'>
                  <StatusIndicator size='LARGE' taskId={feedback.sourceId} />
                </Box>
                <Box width='35%'>
                  <Typography variant='caption' fontWeight={500}>{intl.formatMessage({ id: 'feedback.category' })}</Typography>
                  <Typography>{feedback.labelValue}</Typography>
                </Box>
                <Box width='35%'>
                  <Typography variant='caption' fontWeight={500}>{intl.formatMessage({ id: 'feedback.subCategory' })}</Typography>
                  <Typography>{feedback.subLabelValue}</Typography>
                </Box>
                <Box width='13%'>
                  <Typography variant='caption' fontWeight={500}>{intl.formatMessage({ id: 'feedback.createdBy' })}</Typography>
                  <Typography>{feedback.createdBy}</Typography>
                </Box>
                <Box width='13%'>
                  <Typography variant='caption' fontWeight={500}>{intl.formatMessage({ id: 'feedback.updatedBy' })}</Typography>
                  <Typography>{feedback.updatedBy}</Typography>
                </Box>
              </Box>
            </ListItemButton>
          </ListItem>
          <Divider />
        </>
        )) : <>{intl.formatMessage({ id: 'feedback.none' })}</>}

      </List>
    </Box>

  )
}