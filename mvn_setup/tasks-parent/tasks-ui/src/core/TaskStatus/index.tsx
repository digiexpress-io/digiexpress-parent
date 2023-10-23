import React from 'react';
import { Chip, MenuItem, List, Box, ListItemText } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';

import Client from 'taskclient';
import Context from 'context';
import { TaskDescriptor } from 'taskdescriptor';
import { useMockPopover } from 'core/TaskTable/MockPopover';

const statusColors = Context.StatusPalette;
const statusOptions: Client.TaskStatus[] = ['CREATED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED'];

function getActiveColor(currentlyShowing: Client.TaskStatus, status: Client.TaskStatus): string {
  const selectedTaskStatusColor = statusColors.IN_PROGRESS;
  const color = status === currentlyShowing ? selectedTaskStatusColor : "unset";
  return color;
}

const TaskStatus: React.FC<{ task: TaskDescriptor, onChange: (command: Client.ChangeTaskStatus) => Promise<void> }> = ({ task, onChange }) => {
  const status = task.status;
  const intl = useIntl();
  const Popover = useMockPopover();
  const statusLabel = intl.formatMessage({ id: `tasktable.header.spotlight.status.${status}` }).toUpperCase();

  async function handleStatusChange(newStatus: Client.TaskStatus) {
    const command: Client.ChangeTaskStatus = {
      commandType: 'ChangeTaskStatus',
      status: newStatus,
      taskId: task.id
    };
    onChange(command).then(() => Popover.onClose());
  }
  return (
    <Box>
      <Chip size='small'
        onClick={Popover.onClick}
        label={statusLabel}
      />
      <Popover.Delegate onClose={Popover.onClose}>
        <List dense sx={{ py: 0 }}>
          {statusOptions.map((option: Client.TaskStatus) => (
            <MenuItem key={option} onClick={() => handleStatusChange(option)} sx={{ display: "flex", pl: 0, py: 0 }}>
              <Box sx={{ width: 8, height: 40, backgroundColor: statusColors[option] }} />
              <Box sx={{ width: 8, height: 8, borderRadius: "50%", mx: 2, backgroundColor: getActiveColor(option, status) }} />
              <ListItemText><FormattedMessage id={`task.status.${option}`} /></ListItemText>
            </MenuItem>
          ))}
        </List>
      </Popover.Delegate>
    </Box>
  );
}

export default TaskStatus;