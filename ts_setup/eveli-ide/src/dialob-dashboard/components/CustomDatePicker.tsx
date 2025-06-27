import React from 'react';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { IconButton, InputAdornment, TextField } from '@mui/material';
import { ClearIcon } from '@mui/x-date-pickers';

export interface CustomDatePickerProps {
  value: Date | undefined;
  onChange: (date: Date | null) => void;
  handleDateClear: () => void;
}

const datePickerSx = {
  "& .MuiInputBase-root, MuiOutlinedInput-root": {
    height: "40px",
    paddingRight: "12px"
  }
}

export const CustomDatePicker: React.FC<CustomDatePickerProps> = ({ value, onChange, handleDateClear }) => {
  return (
    <DatePicker
      onChange={onChange}
      value={value}
      slots={{
        textField: (params) => (
          <TextField
            {...params}
            sx={datePickerSx}
            InputProps={{
              ...params.InputProps,
              endAdornment: (
                <InputAdornment position="end">
                  {params.InputProps?.endAdornment}
                  {value && (
                    <IconButton onClick={handleDateClear}>
                      <ClearIcon />
                    </IconButton>
                  )}
                </InputAdornment>
              )
            }}
          />
        )
      }}
    />
  );
};
