import React from 'react';
import { Select, MenuItem, FormControl } from '@mui/material';
import { useIntl } from 'react-intl';
import { TaskApi } from '@dxs-ts/task-api';

export const EditPriority: React.FC<{
  priority: TaskApi.TaskPriority;
  onChange: (priority: TaskApi.TaskPriority) => void;
}> = ({ priority, onChange }) => {
  const intl = useIntl();

  return (
    <FormControl fullWidth hiddenLabel sx={{ mt: 0 }}>

      <Select value={priority} onChange={(e) => onChange(e.target.value as TaskApi.TaskPriority)}>
        {Object.entries(TaskApi.task_priority_messages).map(([key, message]) => (
          <MenuItem key={key} value={key}>
            {intl.formatMessage(message)}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
};

