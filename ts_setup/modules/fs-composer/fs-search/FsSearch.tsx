import React from 'react';
import { Box, Chip, Divider, styled, TextField, Typography, useTheme, FormControl, InputLabel, Select, MenuItem, OutlinedInput, Menu } from '@mui/material';
import { FsColors, getNodeColor, FsNodeType } from '../fs-theme';
import { FilterData } from './search-helpers';

interface FsSearchProps {
  searchTerm: string;
  onSearchChange: (value: string) => void;
  isDarkMode: boolean;
  open: boolean;
  visibleFilters: FilterData[];
  onFiltersChange: (filters: FilterData[]) => void;
}

// All available filter options
const allAvailableFilters: FilterData[] = [
  { label: 'Articles', type: 'article' },
  { label: 'Dialobs', type: 'dialob' },
  { label: 'Services', type: 'service' },
  { label: 'Pages', type: 'folder' },
  { label: 'Links', type: 'link' },
  { label: 'Flows', type: 'flow' },
  { label: 'Printouts', type: 'printout' },
  { label: 'Images', type: 'image' }
];

export const FsSearch: React.FC<FsSearchProps> = ({
  searchTerm,
  onSearchChange,
  isDarkMode,
  open,
  visibleFilters,
  onFiltersChange
}) => {

  if (!open) {
    return;
  }

  function handleSearchChange(event: React.ChangeEvent<HTMLInputElement>) {
    onSearchChange(event.target.value);
  }

  function handleFilterSelectChange(selectedLabels: string[]) {
    const selectedFilters = allAvailableFilters.filter(filter =>
      selectedLabels.includes(filter.label)
    );
    onFiltersChange(selectedFilters);
  }


  if (isDarkMode === true) {
    return (<>
      <Box display='flex' p={1} flexDirection='column' gap={1}>
        <StyledSearchFieldDarkMode placeholder='search' fullWidth value={searchTerm} onChange={handleSearchChange} type='search' />
        <StyledMultiSelectDarkMode multiple
          value={visibleFilters.map(f => f.label)}
          onChange={(e) => handleFilterSelectChange(e.target.value as string[])}
          displayEmpty
          MenuProps={{
            PaperProps: {
              sx: {
                backgroundColor: FsColors.dark.surface,
                color: FsColors.dark.text,
                borderWidth: '1px',
                borderTop: 'unset',
                '& .MuiMenuItem-root': {
                  backgroundColor: FsColors.dark.surface,
                },
                '& .MuiMenuItem-root:hover': {
                  backgroundColor: FsColors.dark.background,
                },
                '& .Mui-selected': {
                  backgroundColor: FsColors.dark.text,
                  color: FsColors.dark.background
                },
                '& .Mui-selected:hover': {
                  backgroundColor: FsColors.dark.surface,
                  color: FsColors.dark.text
                },
              },
            },
          }}
          renderValue={(selected) => {
            if ((selected as string[]).length === 0) {
              return <Typography variant="subtitle2">Filter by type</Typography>;
            }
            return (
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                {(selected as string[]).map((label) => {
                  const filter = allAvailableFilters.find(f => f.label === label);
                  return (
                    <StyledChip
                      key={label}
                      label={label}
                      chipType={filter?.type || 'folder'}
                      isDarkMode={isDarkMode}
                      size="small"
                    />
                  );
                })}
              </Box>
            );
          }}
        >
          {allAvailableFilters.map((filter) => (
            <MenuItem key={filter.type} value={filter.label}>
              {filter.label}
            </MenuItem>
          ))}
        </StyledMultiSelectDarkMode>
      </Box>
      <Divider />
    </>
    )
  }


  return (<>
    <Box display='flex' p={1} flexDirection='column' gap={1} sx={{ backgroundColor: FsColors.light.surface }}>
      <StyledSearchFieldLightMode placeholder='search' fullWidth value={searchTerm} onChange={handleSearchChange} type='search' />
      <StyledMultiSelectLightMode multiple
        value={visibleFilters.map(f => f.label)}
        input={<OutlinedInput sx={{ padding: '0px' }} />}
        onChange={(e) => handleFilterSelectChange(e.target.value as string[])}
        displayEmpty
        renderValue={(selected) => {
          if ((selected as string[]).length === 0) {
            return <Typography variant="subtitle2">Filter by type</Typography>;
          }
          return (
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
              {(selected as string[]).map((label) => {
                const filter = allAvailableFilters.find(f => f.label === label);
                return (
                  <StyledChip key={label} label={label} chipType={filter?.type || 'folder'} isDarkMode={isDarkMode} size="small" />
                );
              })}
            </Box>
          );
        }}
      >
        {allAvailableFilters.map((filter) => (
          <MenuItem key={filter.type} value={filter.label}>
            {filter.label}
          </MenuItem>
        ))}
      </StyledMultiSelectLightMode>
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

const StyledMultiSelectLightMode = styled(Select, {
  shouldForwardProp: (prop) => prop !== 'chipType' && prop !== 'isDarkMode'
})(({ theme }) => ({
  marginTop: '0px',
  borderRadius: 0,
  '& .MuiInputBase-input': {
    padding: theme.spacing(1),
    ...theme.typography.subtitle2,
  },
  '& .MuiOutlinedInput-root': {
    backgroundColor: theme.palette.background.paper,
  },

}))

const StyledMultiSelectDarkMode = styled(Select, {
  shouldForwardProp: (prop) => prop !== 'chipType' && prop !== 'isDarkMode'
})(({ theme }) => ({
  marginTop: 0,
  borderRadius: 0,

  '& .MuiPopover-root .MuiPaper-root': {
    backgroundColor: 'pink'
  },
  '& .MuiOutlinedInput-notchedOutline': {
    borderColor: FsColors.dark.border,
  },
  '&:hover .MuiOutlinedInput-notchedOutline': {
    borderColor: FsColors.dark.text,
  },
  '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
    borderColor: FsColors.dark.text,
    borderWidth: '1px'
  },
  '& .MuiSvgIcon-root': {
    color: FsColors.dark.text,
  },
  '& .MuiInputBase-input': {
    padding: theme.spacing(1),
    ...theme.typography.subtitle2,
    color: FsColors.dark.text,
  },

}));



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
      borderColor: FsColors.dark.text,
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

