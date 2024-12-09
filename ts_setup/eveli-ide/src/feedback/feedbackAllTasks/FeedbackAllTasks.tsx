import React from 'react';
import { Box, Divider, FormControl, List, ListItem, ListItemButton, MenuItem, Select, SelectChangeEvent, Typography, useTheme } from '@mui/material';
import { useIntl } from 'react-intl';
import { useNavigate } from 'react-router-dom';

import * as Burger from '@/burger';
import { useFeedback } from '../feedback-api';
import { StatusIndicator } from '../status-indicator';
import { FeedbackReducer } from './FeedbackReducer';



export interface FeedbackAllTasksProps { }

export const FeedbackAllTasks: React.FC<FeedbackAllTasksProps> = () => {
  const intl = useIntl();
  const navigate = useNavigate();
  const theme = useTheme();
  const { findAllFeedback } = useFeedback();
  const [state, setState] = React.useState(new FeedbackReducer({ data: [] }));


  React.useEffect(() => {
    findAllFeedback().then(data => setState(prev => prev.withData(data)));
  }, []);


  function handleSearch(searchString: string) {
    setState(prev => prev.withSearchBy(searchString))
  }

  function handleChangeCategory(event: SelectChangeEvent<string>) {
    const { value } = event.target;
    setState(prev => prev.withFilterByCategory(value))
  }

  function handleChangeSubCategory(event: SelectChangeEvent<string>) {
    const { value } = event.target;
    setState(prev => prev.withFilterBySubCategory(value))
  }

  function handleFeedbackNav(taskId: string) {
    console.log(taskId)
    navigate(`/feedback/${taskId}`);

  }

  return (
    <Box padding={theme.spacing(3)}>
      <Typography variant='h1'>{intl.formatMessage({ id: 'feedback.all' })}</Typography>

      <Box display='flex' mb={3} gap={1} alignItems='end'>
        <Box width='25%'>
          <Burger.TextField label='feedback.search' onChange={handleSearch} value={state.searchBy ?? ''} placeholder={intl.formatMessage({ id: 'feedback.search.placeholder' })} />
        </Box>

        <Box width='75%'>
          <FormControl sx={{ width: '45%', marginRight: 1 }}>
            <Typography fontWeight='bold'>{intl.formatMessage({ id: 'feedback.search.filter.category' })}</Typography>
            <Select sx={{ padding: 0 }}
              value={state.filterByCategory ?? ''}
              onChange={handleChangeCategory}
            >
              <MenuItem value={''}>{intl.formatMessage({ id: 'feedback.filter.selectNone' })}</MenuItem>

              {state.categories.map((name) => (
                <MenuItem key={name} value={name}>
                  {name}
                </MenuItem>

              ))}
            </Select>
          </FormControl>

          <FormControl sx={{ width: '45%' }}>
            <Typography fontWeight='bold'>{intl.formatMessage({ id: 'feedback.search.filter.subCategory' })}</Typography>
            <Select sx={{ padding: 0 }}
              value={state.filterBySubCategory ?? ''}
              onChange={handleChangeSubCategory}
            >
              <MenuItem value={''}>{intl.formatMessage({ id: 'feedback.filter.selectNone' })}</MenuItem>

              {state.subcategories.map((name) => (
                <MenuItem key={name} value={name}>
                  {name}
                </MenuItem>
              ))}

            </Select>
          </FormControl>

        </Box>
      </Box>

      <List dense disablePadding>

        {state.visibleRows.length ? state.visibleRows.map((feedback) => (
          <React.Fragment key={feedback.id}>
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
          </React.Fragment>
        )) : <>{intl.formatMessage({ id: 'feedback.none' })}</>}

      </List>
    </Box>

  )
}