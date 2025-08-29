import React from 'react';
import { useIntl } from 'react-intl';
import { FormControl, InputLabel, MenuItem, Select, SelectChangeEvent } from '@mui/material';
import { TaskCardStyleKey } from './CardConfigContext';



export interface TaskCardStylerProps {
  value: TaskCardStyleKey;
  onChange: (value: TaskCardStyleKey) => void;
}

export const TaskCardStyleSelect: React.FC<TaskCardStylerProps> = ({ value, onChange }) => {
  const intl = useIntl();
  const handleChange = (event: SelectChangeEvent) => {
    onChange(event.target.value as TaskCardStyleKey);
  };

  return (
    <FormControl fullWidth sx={{ mb: 2, maxWidth: 300 }}>
      <InputLabel>
        {intl.formatMessage({ id: 'taskcard.cardStyle', defaultMessage: 'Card Style' })}
      </InputLabel>
      <Select
        value={value}
        label={intl.formatMessage({ id: 'taskcard.cardStyle', defaultMessage: 'Card Style' })}
        onChange={handleChange}
      >
        <MenuItem value="COMPACT">
          {intl.formatMessage({ id: 'taskcard.style.COMPACT' })}
        </MenuItem>
        <MenuItem value="DEFAULT">
          {intl.formatMessage({ id: 'taskcard.style.DEFAULT' })}
        </MenuItem>
        <MenuItem value="LARGE">
          {intl.formatMessage({ id: 'taskcard.style.LARGE' })}
        </MenuItem>
      </Select>
    </FormControl>
  );
};