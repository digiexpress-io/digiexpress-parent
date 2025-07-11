import React from 'react';
import { Box, Chip } from '@mui/material';

import { TaskApi } from '@/api-task';



export interface TaskPropertiesProps {
  task: TaskApi.Task;
}

export const TaskProperties: React.FC<TaskPropertiesProps> = ({ task }) => {

  if (!task.keyWords) {
    return;
  }


  return (
    <Box gap={1} display='flex' mt={1}>
      {task.keyWords.map(keyword => <Chip label={keyword} variant='filled' color='primary' />)}
    </Box>

  )
}