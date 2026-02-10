import React from 'react';
import { Box, styled, TextField, Typography, useTheme } from '@mui/material';
import { TreeColors } from '../tree-theme';


interface EveliTreeSearchProps {
  searchTerm: string;
  onSearchChange: (value: string) => void;
  isDarkMode: boolean;
}


export const EveliTreeSearch: React.FC<EveliTreeSearchProps> = ({ searchTerm, onSearchChange, isDarkMode }) => {

  function handleSearchChange(event: React.ChangeEvent<HTMLInputElement>) {
    onSearchChange(event.target.value);
  }

  if (isDarkMode === true) {
    return (
      <Box display='flex' p={1}>
        <StyledSearchFieldDarkMode placeholder='search' fullWidth value={searchTerm} onChange={handleSearchChange} type='search' />
      </Box>
    )
  }

  return (
    <Box display='flex' p={1}>
      <StyledSearchFieldLightMode placeholder='search' fullWidth value={searchTerm} onChange={handleSearchChange} type='search' />
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


const StyledSearchFieldLightMode = styled(TextField)(({ theme }) => ({
  marginTop: '0px',
  '& .MuiInputBase-root': {
    borderRadius: 0,
  },
  '& .MuiInputBase-input': {
    padding: theme.spacing(1),
    ...theme.typography.subtitle2
  },
  '& .MuiOutlinedInput-root': {
    backgroundColor: theme.palette.background.paper,
  },
  '& .MuiInputBase-input::placeholder': {
    color: theme.palette.text.secondary,
    opacity: 0.7,
  }
}))


const StyledSearchFieldDarkMode = styled(TextField)(({ theme }) => ({
  marginTop: '0px',
  padding: '0px',
  '& .MuiInputBase-input': {
    padding: theme.spacing(1),
    ...theme.typography.subtitle2,
    color: TreeColors.dark.text
  },
  '& .MuiInputBase-root': {
    backgroundColor: TreeColors.dark.background,
    color: TreeColors.dark.text,
    borderRadius: 0,
    '& fieldset': {
      borderColor: TreeColors.dark.border,
      borderRadius: 0,
    },
    '&:hover fieldset': {
      borderColor: TreeColors.light.textSecondary,
    },
    '&.Mui-focused fieldset': {
      border: `1px solid ${TreeColors.dark.text}`
    },
    '& .MuiInputBase-input::placeholder': {
      color: TreeColors.dark.text,
      opacity: 0.8,
    }
  }
}))
