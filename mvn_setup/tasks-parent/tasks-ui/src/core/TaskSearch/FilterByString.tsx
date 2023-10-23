import React from 'react';
import { TextField, InputAdornment } from '@mui/material';

import SearchIcon from '@mui/icons-material/Search';



const FilterByString: React.FC<{ onChange: (value: React.ChangeEvent<HTMLInputElement>) => void }> = ({ onChange }) => {
  return (
    <TextField
      InputProps={{
        sx: {
          borderRadius: 10,
          width: '40ch',
          height: '2rem',
          '&.MuiOutlinedInput-root': {
            backgroundColor: 'mainContent.main',
            '&.Mui-focused fieldset': {
              borderColor: 'uiElements.main',
              borderWidth: '1px'
            }
          }
        },
        startAdornment: (
          <InputAdornment position="start">
            <SearchIcon sx={{ fontSize: '20px', color: 'uiElements.main' }} />
          </InputAdornment>
        )
      }}
      variant='outlined'
      placeholder='Search'
      onChange={onChange}
    />
  );
}

export default FilterByString;
