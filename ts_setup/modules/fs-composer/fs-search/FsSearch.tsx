import React from 'react';
import { Box, Chip, Divider, styled, TextField, Typography, useTheme } from '@mui/material';
import { FsColors, getNodeColor, FsNodeType } from '../fs-theme';

interface FsSearchProps {
  searchTerm: string;
  onSearchChange: (value: string) => void;
  isDarkMode: boolean;
  open: boolean;
}

interface ChipData {
  label: string;
  type: FsNodeType;
}

export const FsSearch: React.FC<FsSearchProps> = ({ searchTerm, onSearchChange, isDarkMode, open }) => {

  if (!open) {
    return;
  }


  function handleSearchChange(event: React.ChangeEvent<HTMLInputElement>) {
    onSearchChange(event.target.value);
  }

  const [visibleChips, setVisibleChips] = React.useState<ChipData[]>([
    { label: 'Articles', type: 'article' },
    { label: 'Dialobs', type: 'dialob' },
    { label: 'Services', type: 'service' },
    { label: 'Pages', type: 'folder' },
    { label: 'Links', type: 'link' },
    { label: 'Flows', type: 'flow' },
    { label: 'Printouts', type: 'printout' },
    { label: 'Images', type: 'image' }
  ]);

  function handleChipDelete(chipLabel: string) {
    setVisibleChips(prev => prev.filter(chip => chip.label !== chipLabel));
  }


  if (isDarkMode === true) {
    return (<>
      <Box display='flex' p={1} flexDirection='column'>
        <StyledSearchFieldDarkMode placeholder='search' fullWidth value={searchTerm} onChange={handleSearchChange} type='search' />
        <Box display='flex' mt={1} gap={0.5} flexWrap='wrap'>
          {visibleChips.map(chip => (
            <StyledChip
              key={chip.type}
              label={chip.label}
              chipType={chip.type}
              isDarkMode={isDarkMode}
              size='small'
              variant='filled'
              onDelete={() => handleChipDelete(chip.label)}
            />
          ))}
        </Box>
      </Box >
      <Divider />
    </>
    )
  }


  return (<>
    <Box display='flex' p={1} flexDirection='column' sx={{ backgroundColor: FsColors.light.surface }}>
      <StyledSearchFieldLightMode placeholder='search' fullWidth value={searchTerm} onChange={handleSearchChange} type='search' />
      <Box display='flex' mt={1} gap={0.5} flexWrap='wrap'>
        {visibleChips.map(chip => (
          <StyledChip
            key={chip.type}
            label={chip.label}
            chipType={chip.type}
            isDarkMode={isDarkMode}
            size='small'
            variant='filled'
            onDelete={() => handleChipDelete(chip.label)}
          />
        ))}
      </Box>
    </Box>
    <Divider />
  </>
  );
};


export const FsSearchNoResults: React.FC = () => {
  const theme = useTheme();

  return (
    <Box sx={{ px: theme.spacing(2), pt: 0 }}>
      <Typography variant='subtitle2' fontStyle='italic' fontWeight={500}>No results found</Typography>
    </Box>)
}


const StyledSearchFieldLightMode = styled(TextField, {
  shouldForwardProp: (prop) => prop !== 'chipType' && prop !== 'isDarkMode'
})(({ theme }) => ({
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


const StyledSearchFieldDarkMode = styled(TextField, {
  shouldForwardProp: (prop) => prop !== 'chipType' && prop !== 'isDarkMode'
})(({ theme }) => ({

  marginTop: '0px',
  padding: '0px',
  '& .MuiInputBase-input': {
    padding: theme.spacing(1),
    ...theme.typography.subtitle2,
    color: FsColors.dark.text
  },
  '& .MuiInputBase-root': {
    backgroundColor: FsColors.dark.background,
    color: FsColors.dark.text,
    borderRadius: 0,
    '& fieldset': {
      borderColor: FsColors.dark.border,
      borderRadius: 0,
    },
    '&:hover fieldset': {
      borderColor: FsColors.light.textSecondary,
    },
    '&.Mui-focused fieldset': {
      border: `1px solid ${FsColors.dark.text}`
    },
    '& .MuiInputBase-input::placeholder': {
      color: FsColors.dark.text,
      opacity: 0.8,
    }
  }
}))

const StyledChip = styled(Chip, {
  shouldForwardProp: (prop) => prop !== 'chipType' && prop !== 'isDarkMode'
})<{ chipType: FsNodeType; isDarkMode: boolean }>(
  ({ chipType, isDarkMode, theme }) => {
    const baseColor = getNodeColor(chipType, isDarkMode);
    return {
      backgroundColor: baseColor + '20',
      borderColor: baseColor,
      border: `1px solid ${baseColor}`,
      ...theme.typography.caption,
      color: baseColor,
      fontWeight: 'bold',
      '&:hover': {
        backgroundColor: baseColor + '50',
        borderColor: baseColor,
      },
      '& .MuiChip-deleteIcon': {
        color: baseColor,
        '&:hover': {
          color: baseColor,
        }
      }
    };
  }
);

