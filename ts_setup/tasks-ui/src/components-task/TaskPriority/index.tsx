import React from 'react';
import { List, MenuItem, Box, Button, Typography } from '@mui/material';
import { SxProps } from '@mui/system';
import FlagIcon from '@mui/icons-material/Flag';
import { FormattedMessage } from 'react-intl';


import { TaskDescriptor, Palette, TaskPriority, ChangeTaskPriority } from 'descriptor-task';
import { usePopover, TablePopover } from '../TablePopover';

const priorityColors = Palette.priority;
const priorityOptions: TaskPriority[] = ['LOW', 'HIGH', 'MEDIUM'];

function getEmojiFlagSx(priority: TaskPriority): SxProps {
  const color = priorityColors[priority];
  return { color, ':hover': { color } };
}

function getActiveColor(currentlyShowing: TaskPriority, priority: TaskPriority): string {
  const selectedItemColor = Palette.status.IN_PROGRESS;
  const color = priority === currentlyShowing ? selectedItemColor : "unset";
  return color;
}

const TaskPriorityButton: React.FC<{
  task: TaskDescriptor,
  priorityTextEnabled?: boolean,
  onChange: (command: ChangeTaskPriority) => Promise<void>
}> = ({ task, priorityTextEnabled, onChange }) => {
  const priority = task.priority;
  const Popover = usePopover();

  function handlePriorityChange(newPriority: TaskPriority) {
    const command: ChangeTaskPriority = {
      commandType: 'ChangeTaskPriority',
      priority: newPriority,
      taskId: task.id
    }
    onChange(command).then(() => Popover.onClose());
  }


  return (
    <Box>
      <Button color='inherit' onClick={Popover.onClick} sx={{ textTransform: 'none' }}>
        <FlagIcon sx={getEmojiFlagSx(priority)} />
        {priorityTextEnabled && <Typography><FormattedMessage id={'task.priority.' + priority} /></Typography>}
      </Button>
      <TablePopover onClose={Popover.onClose} anchorEl={Popover.anchorEl} open={Popover.open}>
        <List dense sx={{ py: 0 }}>
          {priorityOptions.map((option: TaskPriority) => (
            <MenuItem key={option} onClick={() => handlePriorityChange(option)} sx={{ display: "flex", pl: 0, py: 0 }}>
              <Box sx={{ width: 8, height: 40, backgroundColor: priorityColors[option] }} />
              <Box sx={{ width: 8, height: 8, borderRadius: "50%", mx: 2, backgroundColor: getActiveColor(option, priority) }} />
              <Typography><FormattedMessage id={`task.priority.${option}`} /></Typography>
            </MenuItem>
          ))}
        </List>
      </TablePopover>
    </Box>
  );
}

export default TaskPriorityButton;