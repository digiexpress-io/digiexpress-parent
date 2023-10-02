import React from 'react';
import { Box, useTheme, Typography, SxProps, styled, Badge, BadgeProps } from '@mui/material';
import ChecklistIcon from '@mui/icons-material/Checklist';
import ChatOutlinedIcon from '@mui/icons-material/ChatOutlined';
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

const StyledBadge = styled(Badge)<BadgeProps>(({ theme }) => ({
  '& .MuiBadge-badge': {
    right: -5,
    top: 13,
    border: `2px solid ${theme.palette.background.paper}`,
    padding: '0 4px',
    color: theme.palette.background.paper,
    backgroundColor: theme.palette.uiElements.main
  },
}));


const TaskItem: React.FC<{
  task: Client.TaskDescriptor,
  onTask: (task: Client.TaskDescriptor | undefined) => void,
  active: boolean
}> = ({ task, onTask, active }) => {
  const theme = useTheme();
  const taskDueDate = task.dueDate ? task.dueDate.toLocaleDateString() : undefined;

  const styles = active ? activeTaskStyles : inactiveTaskStyles;

  return (
    <Box sx={styles} display='flex' alignItems='center'
      height={theme.typography.body2.fontSize} maxHeight={theme.typography.body2.fontSize}
      onClick={() => onTask(active ? undefined : task)}>
      <Box width='70%' sx={{ mx: 1 }}><Typography fontWeight='bolder' noWrap>{task.title}</Typography></Box>
      <Box width='10%'>{task.comments.length ? <StyledBadge badgeContent={task.comments.length}><ChatOutlinedIcon /></StyledBadge>: null }</Box>
      <Box width='10%'>{task.checklist.length ? <StyledBadge badgeContent={task.checklist.length}><ChecklistIcon /></StyledBadge> : null}</Box>
      <Box width='10%' sx={{ textAlign: 'right', mx: 1 }}><Typography fontWeight='bolder'>{taskDueDate}</Typography></Box>
    </Box>
  );
}

export default TaskItem;

