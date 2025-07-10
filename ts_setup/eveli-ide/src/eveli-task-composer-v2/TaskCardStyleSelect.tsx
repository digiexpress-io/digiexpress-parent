import React from 'react';
import { TASK_CARD_STYLE_LABELS, TaskCardStyleKey } from './cardThemeConfig';
import { FormControl, InputLabel, MenuItem, Select, SelectChangeEvent } from '@mui/material';



export interface TaskCardStylerProps {
  value: TaskCardStyleKey;
  onChange: (value: TaskCardStyleKey) => void;
}

export const TaskCardStyleSelect: React.FC<TaskCardStylerProps> = ({ value, onChange }) => {
  const handleChange = (event: SelectChangeEvent) => {
    onChange(event.target.value as TaskCardStyleKey);
  };

  return (
    <FormControl fullWidth sx={{ mb: 2, maxWidth: 300 }}>
      <InputLabel>Card Style</InputLabel>
      <Select value={value} label="Card Style" onChange={handleChange}>
        {Object.entries(TASK_CARD_STYLE_LABELS).map(([key, label]) => (
          <MenuItem key={key} value={key}>
            {label}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
};