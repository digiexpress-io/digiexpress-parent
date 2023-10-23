import React from 'react';
import { Box, Typography } from '@mui/material';
import ChecklistIcon from '@mui/icons-material/Checklist';
import ChatOutlinedIcon from '@mui/icons-material/ChatOutlined';

import DaysUntilDue from 'core/DaysUntilDue';
import { TaskDescriptor } from 'taskdescriptor';

const TaskItem: React.FC<{ task: TaskDescriptor }> = ({ task }) => {

  return (
    <Box display='flex' width='100%' alignItems='center'>
      <Box width='50%'><Typography fontWeight='bolder' noWrap>{task.title}</Typography></Box>
      <Box width='30%' justifyItems='left'>
        <Typography fontWeight='bolder'><DaysUntilDue daysUntilDue={task.daysUntilDue} /></Typography>
      </Box>
      <Box width='7%' display='flex' alignItems='center'>{task.comments.length ? <ChatOutlinedIcon sx={{ color: 'uiElements.main' }} /> : null}</Box>
      <Box width='7%' display='flex' alignItems='center'>{task.checklist.length ? <ChecklistIcon sx={{ color: 'uiElements.main' }} /> : null}</Box>
    </Box>
  );
}

export default TaskItem;

