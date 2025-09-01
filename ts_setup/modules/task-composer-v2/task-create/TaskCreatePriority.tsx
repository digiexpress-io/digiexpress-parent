import React from 'react';
import { Select, MenuItem, FormControl, styled } from '@mui/material';
import { useIntl } from 'react-intl';
import { TaskApi } from '@dxs-ts/task-api';

export const EditPriority: React.FC<{
  priority: TaskApi.TaskPriority;
  onChange: (priority: TaskApi.TaskPriority) => void;
}> = ({ priority, onChange }) => {
  const intl = useIntl();

  return (
    <FormControl fullWidth sx={{ marginTop: '4px' }}>
      <StyledSelect value={priority} onChange={(e) => onChange(e.target.value as TaskApi.TaskPriority)}>
        {Object.entries(TaskApi.task_priority_messages).map(([key, message]) => (
          <MenuItem key={key} value={key}>
            {intl.formatMessage(message)}
          </MenuItem>
        ))}
      </StyledSelect>
    </FormControl>
  );
};


export const StyledSelect = styled(Select)(({ theme }) => ({
  height: '2.5rem',

  '& .MuiSelect-select': {
    padding: '0 12px',
    display: 'flex',
    alignItems: 'center',
    height: '100%',
    boxSizing: 'border-box',
  },

  '& .MuiOutlinedInput-notchedOutline': {
    borderColor: theme.palette.divider,
  },
}));