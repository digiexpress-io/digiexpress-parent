import React from 'react';
import { Autocomplete, TextField } from '@mui/material';
import { TaskApi } from '@dxs-ts/task-api';
import { useIntl } from 'react-intl';

export type TaskCreateAssigneeProps = {
  value: TaskApi.User | null;
  userList: TaskApi.User[];
  onChange: (user: TaskApi.User) => void;
};

export const TaskCreateAssignee: React.FC<TaskCreateAssigneeProps> = ({ value, userList, onChange }) => {
  const intl = useIntl();

  return (
    <Autocomplete
      id="assignedUser"
      options={userList}
      value={value}
      getOptionLabel={(option: any) => (typeof option === 'string' ? option : option.userName ?? '')}
      onInputChange={(_event, newInputValue) => {
        if (newInputValue === value?.userName) return;
        const newUser = userList.find((u) => u.userName === newInputValue);
        if (newUser) onChange(newUser);
      }}
      isOptionEqualToValue={(option: any, val: any) =>
        typeof val === 'string' ? false : option.userName === val.userName
      }
      renderInput={(params) => (
        <TextField {...params} fullWidth placeholder={intl.formatMessage({ id: 'task.assigneesAndRolesEdit.assignee.select' })}
          sx={{
            '& .MuiOutlinedInput-root': {
              height: '2.5rem',
              padding: 0,
              '& .MuiAutocomplete-input': {
                paddingLeft: 2, 
              },
            },
            '& .MuiInputBase-input': {
              height: '2.5rem',
              padding: '0 12px',
            },
          }}
        />
      )}
    />
  );
};
