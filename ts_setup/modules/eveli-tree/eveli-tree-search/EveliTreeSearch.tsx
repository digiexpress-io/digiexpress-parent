import React from 'react';
import { Box, TextField } from '@mui/material';



export const EveliTreeSearch: React.FC<{
  searchString: string,
  onSearchChange: (value: string) => void
}> = ({ searchString, onSearchChange }) => {

  function handleSearchChange(event: React.ChangeEvent<HTMLInputElement>) {
    onSearchChange(event.target.value);
  }

  return (
    <Box display='flex' p={1}>
      <TextField
        placeholder='search'
        fullWidth
        value={searchString}
        onChange={handleSearchChange}
      />
    </Box>
  );
};
