import React from 'react';
import { Box, Typography, styled, Badge, BadgeProps } from '@mui/material';
import ChecklistIcon from '@mui/icons-material/Checklist';
import ChatOutlinedIcon from '@mui/icons-material/ChatOutlined';
import Client from '@taskclient';

const StyledBadge = styled(Badge)<BadgeProps>(({ theme }) => ({
  '& .MuiBadge-badge': {
    right: -5,
    top: 15,
    border: `2px solid ${theme.palette.uiElements.main}`,
    padding: '0 4px',
    color: theme.palette.text.primary,
    backgroundColor: theme.palette.uiElements.dark
  },
}));


const TaskItem: React.FC<{ task: Client.TaskDescriptor }> = ({ task }) => {

  const taskDueDate = task.dueDate ? task.dueDate.toLocaleDateString() : undefined;

  return (
    <>
      <Box width='70%' sx={{ mx: 1 }}><Typography fontWeight='bolder' noWrap>{task.title}</Typography></Box>
      <Box width='10%'>{task.comments.length ? <StyledBadge badgeContent={task.comments.length}><ChatOutlinedIcon /></StyledBadge> : null}</Box>
      <Box width='10%'>{task.checklist.length ? <StyledBadge badgeContent={task.checklist.length}><ChecklistIcon /></StyledBadge> : null}</Box>
      <Box width='10%' sx={{ textAlign: 'right', mx: 1 }}><Typography fontWeight='bolder'>{taskDueDate}</Typography></Box>
    </>
  );
}

export default TaskItem;

