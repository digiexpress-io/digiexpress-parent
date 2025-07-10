import React from 'react'
import { FormControl, InputLabel, MenuItem, Select, SelectChangeEvent } from '@mui/material';

export type TaskCardStyleKey = 'compact' | 'default' | 'comfortable';

export interface TaskCardStyleDefinition {
  titleFontSize: string;              // e.g. '1rem', '1.25rem'
  bodyFontSize: string;               // e.g. '0.875rem'
  gridColumnSizes: {
    label: number;                    // value for Grid2 size prop (1–12)
    value: number;
  };
}

export const TaskCardStyleConfig: Record<TaskCardStyleKey, TaskCardStyleDefinition> = {
  compact: {
    titleFontSize: '0.9rem',
    bodyFontSize: '0.75rem',
    gridColumnSizes: {
      label: 3,
      value: 9,
    },
  },
  default: {
    titleFontSize: '1.1rem',
    bodyFontSize: '0.875rem',
    gridColumnSizes: {
      label: 4,
      value: 8,
    },
  },
  comfortable: {
    titleFontSize: '1.25rem',
    bodyFontSize: '1rem',
    gridColumnSizes: {
      label: 5,
      value: 7,
    },
  },
};



export const TASK_CARD_STYLE_LABELS: Record<TaskCardStyleKey, string> = {
  compact: 'Compact',
  default: 'Default',
  comfortable: 'Comfortable',
};


export interface TaskCardStylerProps {
  value: TaskCardStyleKey;
  onChange: (value: TaskCardStyleKey) => void;
}

export const TaskCardStyler: React.FC<TaskCardStylerProps> = ({ value, onChange }) => {
  const handleChange = (event: SelectChangeEvent) => {
    onChange(event.target.value as TaskCardStyleKey);
  };

  return (
    <FormControl fullWidth sx={{ mb: 2, maxWidth: 300 }}>
      <InputLabel id="style-preset-label">Card Style</InputLabel>
      <Select
        labelId="style-preset-label"
        id="style-preset-select"
        value={value}
        label="Card Style"
        onChange={handleChange}
      >
        {Object.entries(TASK_CARD_STYLE_LABELS).map(([key, label]) => (
          <MenuItem key={key} value={key}>
            {label}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
};
