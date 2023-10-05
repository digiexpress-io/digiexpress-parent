import React from 'react';
import { Box, useTheme, Typography, SxProps, styled, Badge, BadgeProps, alpha } from '@mui/material';
import ChecklistIcon from '@mui/icons-material/Checklist';
import ChatOutlinedIcon from '@mui/icons-material/ChatOutlined';
import Client from '@taskclient';


const inactiveEvenTaskStyles: SxProps = {
  p: 2,
  cursor: 'pointer',
  backgroundColor: 'mainContent.main',
  color: 'text.primary'
}

const inactiveOddTaskStyles: SxProps = {
  p: 2,
  cursor: 'pointer',
  backgroundColor: 'uiElements.light',
  color: 'text.primary'
}


const StyledTaskRow: React.FC<{
  index: number,
  children: React.ReactNode,
  onClick: () => void,
  active: boolean
}> = ({ active, onClick, children, index }) => {
  const theme = useTheme();
  const isEven = index % 2 === 0;
  const isOdd = index % 2 === 1;

  const activeTaskStyles: SxProps = {
    p: 2,
    cursor: 'pointer',
    color: 'text.primary',
    backgroundColor: alpha(theme.palette.uiElements.main, 0.3),
    fontWeight: 'bolder',
    borderRadius: 1
  }

  function getStyles(_index: number) {
    if (isOdd) {
      if (active) {
        return activeTaskStyles;
      }
      return inactiveOddTaskStyles;
    }
    else if (isEven) {
      if (active) {
        return activeTaskStyles;
      }
      return inactiveEvenTaskStyles;
    }
  }


  return (
    <Box sx={getStyles(index)} display='flex' alignItems='center'
      height={theme.typography.body2.fontSize} maxHeight={theme.typography.body2.fontSize} onClick={onClick}>
      {children}
    </Box>);
}

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


const TaskItem: React.FC<{
  task: Client.TaskDescriptor,
  onTask: (task: Client.TaskDescriptor | undefined) => void,
  active: boolean,
  index: number
}> = ({ task, onTask, active, index }) => {

  const taskDueDate = task.dueDate ? task.dueDate.toLocaleDateString() : undefined;


  return (
    <StyledTaskRow index={index} onClick={() => onTask(active ? undefined : task)} active={active} >
      <Box width='70%' sx={{ mx: 1 }}><Typography fontWeight='bolder' noWrap>{task.title}</Typography></Box>
      <Box width='10%'>{task.comments.length ? <StyledBadge badgeContent={task.comments.length}><ChatOutlinedIcon /></StyledBadge> : null}</Box>
      <Box width='10%'>{task.checklist.length ? <StyledBadge badgeContent={task.checklist.length}><ChecklistIcon /></StyledBadge> : null}</Box>
      <Box width='10%' sx={{ textAlign: 'right', mx: 1 }}><Typography fontWeight='bolder'>{taskDueDate}</Typography></Box>
    </StyledTaskRow>
  );
}

export default TaskItem;

