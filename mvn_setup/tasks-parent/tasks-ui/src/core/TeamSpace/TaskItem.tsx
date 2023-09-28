import React from 'react';
import { Box, useTheme, Typography, SxProps } from '@mui/material';
import Client from '@taskclient';



const activeTaskStyles: SxProps = {
  color: 'mainContent.main',
  backgroundColor: 'uiElements.main',
  fontWeight: 'bold',
  borderRadius: 1,
  p: 2,
  cursor: 'pointer'
}

const inactiveTaskStyles: SxProps = {
  p: 2,
  cursor: 'pointer'
}

const TaskItem: React.FC<{
  task: Client.TaskDescriptor,
  onTask: (task: Client.TaskDescriptor | undefined) => void,
  active: boolean
}> = ({ task, onTask, active }) => {
  const theme = useTheme();
  const taskDueDate = task.dueDate ? task.dueDate.toLocaleDateString() : undefined;
  const isCompletedOrRejected: boolean = task.status === 'COMPLETED' || task.status === 'REJECTED';

  if (isCompletedOrRejected) {
    return <></>;
  }

  const styles = active ? activeTaskStyles : inactiveTaskStyles;

  return (
    <Box sx={styles} display='flex' alignItems='center'
      height={theme.typography.body2.fontSize} maxHeight={theme.typography.body2.fontSize}
      onClick={() => onTask(active ? undefined : task)}>
      <Box sx={{ mx: 2 }} />
      <Box width='50%' maxWidth='50%'><Typography fontWeight='bolder' noWrap>{task.title}</Typography></Box>
      <Box width='50%' sx={{ textAlign: 'right' }}><Typography>{taskDueDate}</Typography></Box>
    </Box>
  );
}

export default TaskItem ;

