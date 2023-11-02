import React from 'react';
import { SxProps, List, MenuItem, Box, Button, Typography } from '@mui/material';
import FlagIcon from '@mui/icons-material/Flag';
import { FormattedMessage } from 'react-intl';

import Client from 'client';
import { TaskDescriptor } from 'taskdescriptor';
import Context from 'context';
import { useMockPopover } from 'taskcomponents/TaskTable/MockPopover';

const priorityColors = Context.PriorityPalette;
const priorityOptions: Client.TaskPriority[] = ['LOW', 'HIGH', 'MEDIUM'];

function getEmojiFlagSx(priority: Client.TaskPriority): SxProps {
  const color = priorityColors[priority];
  return { color, ':hover': { color } };
}

function getActiveColor(currentlyShowing: Client.TaskPriority, priority: Client.TaskPriority): string {
  const selectedItemColor = Context.StatusPalette.IN_PROGRESS;
  const color = priority === currentlyShowing ? selectedItemColor : "unset";
  return color;
}

const TaskPriority: React.FC<{
  task: TaskDescriptor,
  priorityTextEnabled?: boolean,
  onChange: (command: Client.ChangeTaskPriority) => Promise<void>
}> = ({ task, priorityTextEnabled, onChange }) => {
  const priority = task.priority;
  const Popover = useMockPopover();

  function handlePriorityChange(newPriority: Client.TaskPriority) {
    const command: Client.ChangeTaskPriority = {
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
      <Popover.Delegate onClose={Popover.onClose}>
        <List dense sx={{ py: 0 }}>
          {priorityOptions.map((option: Client.TaskPriority) => (
            <MenuItem key={option} onClick={() => handlePriorityChange(option)} sx={{ display: "flex", pl: 0, py: 0 }}>
              <Box sx={{ width: 8, height: 40, backgroundColor: priorityColors[option] }} />
              <Box sx={{ width: 8, height: 8, borderRadius: "50%", mx: 2, backgroundColor: getActiveColor(option, priority) }} />
              <Typography><FormattedMessage id={`task.priority.${option}`} /></Typography>
            </MenuItem>
          ))}
        </List>
      </Popover.Delegate>
    </Box>
  );
}

export default TaskPriority;