import React from 'react';
import { alpha, Box, useTheme, Typography, Avatar } from '@mui/material';
import PriorityHighIcon from '@mui/icons-material/PriorityHigh';

import { TaskApi } from '@/api-task';
import { DateTime } from 'luxon';
import { TaskCardStyleDefinition } from './cardThemeConfig';



export const TaskOverdueWarning: React.FC<{ task: TaskApi.Task, style: TaskCardStyleDefinition }> = ({ task, style }) => {
  const theme = useTheme();
  if (!task.dueDate) {
    return;
  }

  const today = DateTime.local().startOf('day');
  const dueDate = DateTime.fromJSDate(task.dueDate).startOf('day');
  const diffInDays = Math.floor(dueDate.diff(today, 'days').days);

  if (task.completed) {
    const completedDate = DateTime.fromJSDate(task.completed).startOf('day');
    const daysOverdue = Math.floor(completedDate.diff(dueDate, 'days').days);

    if (daysOverdue > 0) {
      return (
        <Box sx={{
          display: 'flex',
          alignItems: 'center',
          backgroundColor: alpha(theme.palette.warning.main, 0.2),
          borderRadius: theme.spacing(3),
          pr: theme.spacing(2)
        }}>
          <Avatar sx={{ backgroundColor: theme.palette.warning.main, color: theme.palette.background.default, mr: theme.spacing(1) }}>
            <PriorityHighIcon />
          </Avatar>
          <Typography sx={{ ...style.bodyTypography, fontWeight: 'bold', color: theme.palette.warning.dark }}>
            This task was closed {daysOverdue} day(s) overdue
          </Typography>
        </Box>
      );
    }
  } else if (diffInDays < 0) {
    return (
      <Box sx={{
        display: 'flex',
        alignItems: 'center',
        backgroundColor: alpha(theme.palette.error.main, 0.2),
        borderRadius: theme.spacing(3),
        pr: theme.spacing(2)
      }}>
        <Avatar sx={{ backgroundColor: theme.palette.error.main, color: theme.palette.background.default, mr: theme.spacing(1) }}><PriorityHighIcon /></Avatar>
        <Typography sx={{ ...style.bodyTypography, color: theme.palette.error.main, fontWeight: 'bold' }}>This task is {-diffInDays} day(s) overdue</Typography>
      </Box>
    );
  } else if (diffInDays === 0) {
    return (
      <Box sx={{
        display: 'flex',
        alignItems: 'center',
        backgroundColor: alpha('#ffea00', 0.2),
        borderRadius: theme.spacing(3),
        pr: theme.spacing(2)
      }}>
        <Avatar sx={{ backgroundColor: '#ffea00', color: theme.palette.text.primary, mr: theme.spacing(1) }}><PriorityHighIcon /></Avatar>
        <Typography textAlign='center' sx={{ ...style.bodyTypography, color: theme.palette.text.primary, fontWeight: 'bold' }}>Task is due today!</Typography>
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
      <Typography textAlign='center' sx={{ ...style.bodyTypography, color: theme.palette.info.dark, fontWeight: 'bold' }}>{diffInDays} day(s) left to complete this task</Typography>
    </Box>
  );

};