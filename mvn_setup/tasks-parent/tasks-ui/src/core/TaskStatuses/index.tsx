import React from 'react';
import { Chip, MenuItem, List, Box, ListItemText } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';

import Client from '@taskclient';
import { useMockPopover } from 'core/TaskTable/MockPopover';

function getStatusBackgroundColor(status: Client.TaskStatus): string {
  const color = Client.StatusPallette[status] || "unset";
  return color;
}

const TaskStatuses: React.FC<{ task: Client.TaskDescriptor }> = ({ task }) => {
  const intl = useIntl();
  const Popover = useMockPopover();
  const statusLabel = intl.formatMessage({ id: `tasktable.header.spotlight.status.${task.status}` }).toUpperCase();
  const statusOptions: Client.TaskStatus[] = ['CREATED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED'];

  function getActiveColor(option: Client.TaskStatus): string {
    const color = task.status === option ? Client.StatusPallette.IN_PROGRESS : "unset";
    return color;
  }

  return (
    <Box>
      <Chip 
        onClick={Popover.onClick} 
        label={statusLabel}
        sx={{
          backgroundColor: getStatusBackgroundColor(task.status), 
          color: "primary.contrastText",
          ml: 0,
          ":hover": { backgroundColor: "#404c64"}
        }} 
      />
      <Popover.Delegate>
        <List dense sx={{ py: 0 }}>
          {statusOptions.map((option: Client.TaskStatus) => (
            <MenuItem key={option} onClick={Popover.onClose} sx={{ display: "flex", pl: 0, py: 0 }}>
              <Box sx={{ width: 8, height: 40, backgroundColor: getStatusBackgroundColor(option)}} />
              <Box sx={{ width: 8, height: 8, borderRadius: "50%", mx: 2, backgroundColor: getActiveColor(option)}} />
              <ListItemText><FormattedMessage id={`task.status.${option}`} /></ListItemText>
            </MenuItem>
          ))}
        </List>
      </Popover.Delegate>
    </Box>
  );
}

export default TaskStatuses;