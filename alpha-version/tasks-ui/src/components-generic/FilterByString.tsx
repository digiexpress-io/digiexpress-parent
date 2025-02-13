import React from 'react';
import { TextField, InputAdornment } from '@mui/material';

import SearchIcon from '@mui/icons-material/Search';
import { blue_mud, cyan, wash_me } from 'components-colors';

const FilterByString: React.FC<{ defaultValue?: string, onChange: (value: React.ChangeEvent<HTMLInputElement>) => void }> = ({ defaultValue, onChange }) => {
  return (
    <TextField
      defaultValue={defaultValue}
      variant='outlined'
      placeholder='Search'
      onChange={onChange}
      InputProps={{
        sx: {
          borderRadius: 10,
          width: '40ch',
          height: '2rem',
          '&.MuiOutlinedInput-notchedOutline': {
            border: '1px solid ' + blue_mud,
          },
          '&.MuiInputBase-root': {
            border: 'unset'
          },
          '&.MuiOutlinedInput-root': {
            backgroundColor: wash_me,

            '&.Mui-focused fieldset': {
              border: '1px solid ' + blue_mud
            },
            '&:hover fieldset': {
              borderColor: blue_mud,
            },
          }
        },
        startAdornment: (
          <InputAdornment position="start">
            <SearchIcon sx={{ fontSize: '20px', color: cyan }} />
          </InputAdornment>
        ),
      }}
    />
  );
}

export default FilterByString;
