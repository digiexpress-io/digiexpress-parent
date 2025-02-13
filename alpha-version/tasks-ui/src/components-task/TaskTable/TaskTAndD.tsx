import React from 'react';


import { Box, Typography } from '@mui/material';
import { TaskDescriptor } from 'descriptor-task';


export const TaskTAndD: React.FC<{ task: TaskDescriptor }> = ({task}) => {
  return (
    <Box display="flex" flexDirection="column">
    <Typography variant='h5'>{task.title}</Typography>
    <Box sx={{ pt: 1 }} />
    <Typography variant='caption'>{task.description}</Typography>
  </Box>
  )
}
