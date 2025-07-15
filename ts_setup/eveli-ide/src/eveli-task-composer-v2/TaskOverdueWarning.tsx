import React from 'react';
import { alpha, Box, useTheme, Typography, Avatar } from '@mui/material';
import PriorityHighIcon from '@mui/icons-material/PriorityHigh';

import { TaskApi } from '@/api-task';
import { DateTime } from 'luxon';



export const TaskOverdueWarning: React.FC<{ task: TaskApi.Task }> = ({ task }) => {
  const theme = useTheme();
  if (!task.dueDate) {
    return;
  }

  const today = DateTime.local().startOf('day');
  const dueDate = DateTime.fromJSDate(task.dueDate).startOf('day');
  const diffInDays = Math.floor(dueDate.diff(today, 'days').days);

  if (diffInDays < 0) {
    return (
      <Box sx={{
        display: 'flex',
        alignItems: 'center',
        backgroundColor: alpha(theme.palette.error.main, 0.2),
        borderRadius: theme.spacing(3),
        pr: theme.spacing(2)
      }}>
        <Avatar sx={{ backgroundColor: theme.palette.error.main, color: theme.palette.background.default, mr: theme.spacing(1) }}><PriorityHighIcon /></Avatar>
        <Typography fontWeight='bold' variant='body1' color={theme.palette.error.main}>This task is {-diffInDays} day(s) overdue</Typography>

      </Box>
    );
  }

  if (diffInDays === 0) {
    return (
      <Box sx={{
        display: 'flex',
        alignItems: 'center',
        backgroundColor: alpha('#ffea00', 0.2),
        borderRadius: theme.spacing(3),
        pr: theme.spacing(2)
      }}>
        <Avatar sx={{ backgroundColor: '#ffea00', color: theme.palette.text.primary, mr: theme.spacing(1) }}><PriorityHighIcon /></Avatar>
        <Typography fontWeight='bold' variant='body1' textAlign='center'>Task is due today!</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{
      display: 'flex',
      alignItems: 'center',
      backgroundColor: alpha(theme.palette.info.main, 0.2),
      borderRadius: theme.spacing(3),
      pr: theme.spacing(2)
    }}>
      <Avatar sx={{ backgroundColor:theme.palette.info.main, color: theme.palette.background.default, mr: theme.spacing(1) }}><PriorityHighIcon /></Avatar>
      <Typography fontWeight='bold' variant='body1' textAlign='center' color={theme.palette.info.dark}>{diffInDays} day(s) left to complete this task</Typography>
    </Box>
  );

};