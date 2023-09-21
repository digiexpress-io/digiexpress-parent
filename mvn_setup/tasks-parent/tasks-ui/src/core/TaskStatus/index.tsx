import React from 'react';
import { Chip, MenuItem, List, Box, ListItemText } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';

import Client from '@taskclient';
import { useMockPopover } from 'core/TaskTable/MockPopover';

const statusColors = Client.StatusPallette;
const statusOptions: Client.TaskStatus[] = ['CREATED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED'];

function getActiveColor(currentlyShowing: Client.TaskStatus, status: Client.TaskStatus): string {
  const selectedTaskStatusColor = statusColors.IN_PROGRESS;
  const color = status === currentlyShowing ? selectedTaskStatusColor : "unset";
  return color;
}

const TaskStatus: React.FC<{ task: Client.TaskDescriptor }> = ({ task }) => {
  const status = task.status;
  const intl = useIntl();
  const Popover = useMockPopover();
  const statusLabel = intl.formatMessage({ id: `tasktable.header.spotlight.status.${status}` }).toUpperCase();

  return (
    <Box>
      <Chip 
        onClick={Popover.onClick} 
        label={statusLabel}
        sx={{
          backgroundColor: statusColors[status], 
          color: "primary.contrastText",
          ml: 0,
          ":hover": { backgroundColor: "#404c64"}
        }} 
      />
      <Popover.Delegate>
        <List dense sx={{ py: 0 }}>
          {statusOptions.map((option: Client.TaskStatus) => (
            <MenuItem key={option} onClick={Popover.onClose} sx={{ display: "flex", pl: 0, py: 0 }}>
              <Box sx={{ width: 8, height: 40, backgroundColor: statusColors[option]}} />
              <Box sx={{ width: 8, height: 8, borderRadius: "50%", mx: 2, backgroundColor: getActiveColor(option, status)}} />
              <ListItemText><FormattedMessage id={`task.status.${option}`} /></ListItemText>
            </MenuItem>
          ))}
        </List>
      </Popover.Delegate>
    </Box>
  );
}

export default TaskStatus;