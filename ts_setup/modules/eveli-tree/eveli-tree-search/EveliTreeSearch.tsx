import React from 'react';
import { Box, TextField, Typography, useTheme } from '@mui/material';


interface EveliTreeSearchProps {
  searchTerm: string;
  onSearchChange: (value: string) => void;
}


export const EveliTreeSearch: React.FC<EveliTreeSearchProps> = ({ searchTerm, onSearchChange }) => {

  function handleSearchChange(event: React.ChangeEvent<HTMLInputElement>) {
    onSearchChange(event.target.value);
  }

  return (
    <Box display='flex' p={1}>
      <TextField placeholder='search' fullWidth value={searchTerm} onChange={handleSearchChange} type='search' />
    </Box>
  );
};


export const EveliTreeSearchNoResults: React.FC = () => {
  const theme = useTheme();

  return (
    <Box sx={{ px: theme.spacing(2), pt: 0 }}>
      <Typography variant='subtitle2' fontStyle='italic' fontWeight={500}>No results found</Typography>
    </Box>)
}