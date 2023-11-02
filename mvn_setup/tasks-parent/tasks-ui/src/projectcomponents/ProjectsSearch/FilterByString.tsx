import React from 'react';
import { TextField, InputAdornment } from '@mui/material';

import SearchIcon from '@mui/icons-material/Search';

const FilterByString: React.FC<{ onChange: (value: React.ChangeEvent<HTMLInputElement>) => void }> = ({ onChange }) => {
  return (
    <TextField
      variant='outlined'
      placeholder='Search'
      onChange={onChange}
      InputProps={{
        sx: {
          borderRadius: 10,
          width: '40ch',
          height: '2rem',
          '&.MuiOutlinedInput-notchedOutline': {
            border: '1px solid rgba(96, 113, 150, 0.5)',
          },
          '&.MuiInputBase-root': {
            border: 'unset'
          },
          '&.MuiOutlinedInput-root': {
            backgroundColor: 'mainContent.main',

            '&.Mui-focused fieldset': {
              border: '1px solid rgba(96, 113, 150, 0.5)'
            },
            '&:hover fieldset': {
              borderColor: 'rgba(96, 113, 150, 0.5)',
            },
          }
        },
        startAdornment: (
          <InputAdornment position="start">
            <SearchIcon sx={{ fontSize: '20px', color: 'uiElements.main' }} />
          </InputAdornment>
        ),
      }}
    />
  );
}

export default FilterByString;
