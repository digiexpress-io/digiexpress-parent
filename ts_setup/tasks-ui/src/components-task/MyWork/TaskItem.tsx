import React from 'react';
import { Box, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import ChecklistIcon from '@mui/icons-material/Checklist';
import ChatOutlinedIcon from '@mui/icons-material/ChatOutlined';

import DaysUntilDue from '../DaysUntilDue';
import { TaskDescriptor } from 'descriptor-task';
import { cyan } from 'components-colors';


const TaskItem: React.FC<{ task: TaskDescriptor }> = ({ task }) => {

  const dueDate = task.daysUntilDue ?
    (<Typography fontWeight='bolder'><DaysUntilDue daysUntilDue={task.daysUntilDue} /></Typography>)
    :
    (<Typography fontStyle='italic'><FormattedMessage id='task.dueDate.none' /></Typography>);


  return (
    <Box display='flex' width='100%' alignItems='center'>
      <Box width='50%'><Typography fontWeight='bolder' noWrap>{task.title}</Typography></Box>
      <Box width='30%' justifyItems='left'>{dueDate}</Box>
      <Box width='7%' display='flex' alignItems='center'>{task.comments.length ? <ChatOutlinedIcon sx={{ color: cyan }} /> : null}</Box>
      <Box width='7%' display='flex' alignItems='center'>{task.checklist.length ? <ChecklistIcon sx={{ color: cyan }} /> : null}</Box>
    </Box>
  );
}

export default TaskItem;

