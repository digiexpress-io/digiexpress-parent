import React from 'react';
import { SxProps, MenuList, MenuItem, ListItemText, Box, Button, Typography } from '@mui/material';
import EmojiFlagsIcon from '@mui/icons-material/EmojiFlags';
import { FormattedMessage } from 'react-intl';

import Client from '@taskclient';
import { usePopover } from 'core/TaskTable/CellPopover';

const priorityColors = Client.PriorityPalette;
const priorityOptions: Client.TaskPriority[] = ['LOW', 'HIGH', 'MEDIUM'];

function getPrioritySx(priority: Client.TaskPriority): SxProps{
  const color = priorityColors[priority];
  return { color, ':hover': { color }, mr: 1 };
}

const TaskPriorities: React.FC<{ task: Client.TaskDescriptor, showPriorityText?: boolean }> = ({ task, showPriorityText }) => {
  const priority = task.priority;
  const Popover = usePopover();

  return (
    <Box>
      <Button variant='text' color='inherit' onClick={Popover.onClick} sx={{ textTransform: 'none' }}>
        <EmojiFlagsIcon sx={getPrioritySx(priority)} />
        {showPriorityText && <Typography><FormattedMessage id={'task.priority.' + priority} /></Typography>}
      </Button>
      <Popover.Delegate>
        <MenuList dense>
          {priorityOptions.map((option: Client.TaskPriority) => (
            <MenuItem key={option} onClick={Popover.onClose}>
              <EmojiFlagsIcon sx={getPrioritySx(option)} />
              <ListItemText><FormattedMessage id={'task.priority.' + option} /></ListItemText>
            </MenuItem>
          ))}
        </MenuList>
      </Popover.Delegate>
    </Box>
  );
}

export default TaskPriorities;