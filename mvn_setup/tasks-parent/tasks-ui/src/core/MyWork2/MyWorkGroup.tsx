import React from 'react';
import { Box, useTheme, Typography, IconButton } from '@mui/material';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';

import Client from '@taskclient';
import { FormattedMessage } from 'react-intl';

const groupTitles: Record<string, string> = {
  assigneeOverdue: "Overdue work",
  assigneeOther: "Assigned to me",
  assigneeStartsToday: "Starts today"
}

const StyledTaskItem: React.FC<{ task: Client.TaskDescriptor }> = ({ task }) => {
  const theme = useTheme();
  const status = task.status;
  const groupId = task.assigneeGroupType;
  const [active, setActive] = React.useState(false);

  function handleMouseOver(){
    setActive(true)
  }

  function handleMouseLeave(){
    setActive(false)
  }

  function formatTaskDate(date: Date | undefined): string | undefined {
    return date?.toLocaleDateString();
  }

  const taskDate = (
    <Typography sx={{color: groupId === 'assigneeOverdue' ? Client.AssigneePalette.assigneeOverdue : undefined}}>
      { groupId === "assigneeStartsToday" ? formatTaskDate(task.startDate) : formatTaskDate(task.dueDate)}
    </Typography>
  );
  

  return (
    <Box 
      my={2} 
      display='flex' 
      alignItems='center'
      height={theme.typography.body2.fontSize} 
      maxHeight={theme.typography.body2.fontSize}
      onMouseOver={handleMouseOver}
      onMouseLeave={handleMouseLeave}
    >
      <Box width='45%' maxWidth="45%">
        <Typography noWrap>{task.title}</Typography>
      </Box>
      <Box width='30%'>
        <Typography><FormattedMessage id={`task.status.${status}`} /></Typography>
      </Box>
      <Box width='20%'>{taskDate}</Box>
      <Box>{active && <IconButton><MoreHorizIcon /></IconButton>}</Box>
    </Box>
  );
}

const TaskGroup: React.FC<{ group: Client.Group }> = ({ group }) => {
  const emptyGroupTitle = group.records.length === 0 && <Typography><FormattedMessage id={"core.myWork.group.empty.title"} /></Typography>;

  return (
    <Box>
      <GroupTitle group={group}/>
      {emptyGroupTitle}
      {group.records.map((task) => <StyledTaskItem key={task.id} task={task} />)}
    </Box>
  );
}

const GroupTitle: React.FC<{ group: Client.Group }> = ({ group }) => {
  return (
    <Box width='50%' mt={2} mb={4}>
      <Typography variant='h4' fontWeight='bold'>
        {groupTitles[group.id]}
      </Typography>
    </Box>
  )
}

export default TaskGroup;