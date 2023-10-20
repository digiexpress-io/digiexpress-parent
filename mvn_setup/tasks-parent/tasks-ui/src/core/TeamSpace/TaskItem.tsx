import React from 'react';
import { Box, Typography } from '@mui/material';
import ChecklistIcon from '@mui/icons-material/Checklist';
import ChatOutlinedIcon from '@mui/icons-material/ChatOutlined';
import { FormattedMessage } from 'react-intl';

import Client from '@taskclient';



function formatDaysUntilDue(task: Client.TaskDescriptor): { isOverdue: boolean, daysUntilDue?: number } {
  if (task.daysUntilDue === undefined) {
    return { isOverdue: false };
  }

  if (task.daysUntilDue < 0) {
    let daysToFormat = task.daysUntilDue; // daysUntilDue is a negative number if task is overdue, requires formatting to positive for display purposes
    const overdueDays = daysToFormat *= -1;
    return { daysUntilDue: overdueDays, isOverdue: true };
  }
  return { daysUntilDue: task.daysUntilDue, isOverdue: false };
}

const TaskItem: React.FC<{ task: Client.TaskDescriptor }> = ({ task }) => {

  const days = formatDaysUntilDue(task);
  const id = days.isOverdue ? 'core.teamSpace.task.daysUntilDue.overdueBy' : 'core.teamSpace.task.daysUntilDue';

  return (
    <Box display='flex' width='100%' alignItems='center'>
      <Box width='50%'><Typography fontWeight='bolder' noWrap>{task.title}</Typography></Box>
      <Box width='30%' justifyItems='left'>
        <Typography fontWeight='bolder'><FormattedMessage id={id} values={{ count: days.daysUntilDue }} /></Typography>
      </Box>
      <Box width='7%' display='flex' alignItems='center'>{task.comments.length ? <ChatOutlinedIcon sx={{ color: 'uiElements.main' }} /> : null}</Box>
      <Box width='7%' display='flex' alignItems='center'>{task.checklist.length ? <ChecklistIcon sx={{ color: 'uiElements.main' }} /> : null}</Box>
    </Box>
  );
}

export default TaskItem;

