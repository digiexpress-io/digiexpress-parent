import React from 'react';
import { Box, TextField, Typography, FormControl, InputLabel, Select, MenuItem, Chip, OutlinedInput, styled } from '@mui/material';
import {
  Construction as DevModeIcon,
  Assignment as AssignmentIcon,
  Block as DisabledIcon,
  VisibilityOff as AnonymousIcon,
} from '@mui/icons-material';

const configOptions = [
  { value: 'devMode', label: 'Development', icon: <DevModeIcon fontSize='small' /> },
  { value: 'assignableMode', label: 'Assignable', icon: <AssignmentIcon fontSize='small' /> },
  { value: 'disabledMode', label: 'Disabled', icon: <DisabledIcon fontSize='small' /> },
  { value: 'anonymousMode', label: 'Anonymous', icon: <AnonymousIcon fontSize='small' /> },
];

const mockSelectedValues = ['devMode', 'assignableMode', 'disabledMode', 'anonymousMode'];

export const NewItem: React.FC = () => {
  return (
    <>
      <StyledCaption variant='caption'>Create new item</StyledCaption>
      <StyledFormContainer>
        <StyledTextField
          label="Name"
          size='small'
          fullWidth
        />

        <StyledTextField
          label="Order Number"
          size='small'
          fullWidth
        />

        <StyledTextField
          label="Description"
          size='small'
          fullWidth
          multiline
          rows={2}
        />

        <StyledFormControl fullWidth size='small'>
          <StyledSelect
            multiple
            value={mockSelectedValues}
            input={<OutlinedInput />}
            renderValue={(selected) => (
              <StyledChipContainer>
                {(selected as string[]).map((value) => {
                  const option = configOptions.find(opt => opt.value === value);
                  return (
                    <StyledChip
                      key={value}
                      icon={option?.icon}
                      label={option?.label}
                      size="small"
                      variant='outlined'
                    />
                  );
                })}
              </StyledChipContainer>
            )}
          >
            {configOptions.map((option) => (
              <StyledMenuItem key={option.value} value={option.value}>
                <StyledMenuItemContent>
                  {option.icon}
                  {option.label}
                </StyledMenuItemContent>
              </StyledMenuItem>
            ))}
          </StyledSelect>
        </StyledFormControl>

        <StyledTextField
          label="Labels"
          size='small'
          fullWidth
          multiline
          rows={2}
          placeholder="Enter labels separated by commas"
        />

        <StyledTextField
          label="Comments"
          size='small'
          fullWidth
          multiline
          rows={3}
        />

        <StyledSectionBox>
          <StyledSectionTitle variant='caption'>Sharing and Permissions</StyledSectionTitle>
          <StyledSectionContent variant='body2'>Put content here</StyledSectionContent>
        </StyledSectionBox>
      </StyledFormContainer>
    </>
  );
};

const StyledCaption = styled(Typography)(() => ({
  color: '#cccccc',
}));

const StyledFormContainer = styled(Box)(() => ({
  display: 'flex',
  flexDirection: 'column',
  gap: '12px',
  padding: '8px 0',
}));

const StyledTextField = styled(TextField)(({ theme }) => ({
  width: '100%',
  marginTop: '5px !important',
  '& .MuiInputBase-root': {
    backgroundColor: '#1e1e1e',
    color: '#cccccc',
    borderRadius: 0,
    '& fieldset': {
      borderColor: '#3c3c3c',
      borderRadius: 0,
    },
    '&:hover fieldset': {
      borderColor: '#555555',
    },
    '&.Mui-focused fieldset': {
      borderColor: theme.palette.primary.main,
    },
  },
  '& .MuiInputBase-input': {
    color: '#cccccc',
    ...theme.typography.caption,
    padding: '8px 12px',
    '&::placeholder': {
      color: '#888888',
      opacity: 1,
      ...theme.typography.caption,
    },
  },
  '& .MuiInputLabel-root': {
    color: '#cccccc',
    ...theme.typography.caption,
  },
}));

const StyledFormControl = styled(FormControl)(({ theme }) => ({
  width: '100%',
  marginTop: '5px !important',
}));

const StyledSelect = styled(Select)(({ theme }) => ({
  backgroundColor: '#1e1e1e',
  color: '#cccccc',
  borderRadius: 0,
  '& .MuiOutlinedInput-notchedOutline': {
    borderColor: '#3c3c3c',
    borderRadius: 0,
  },
  '&:hover .MuiOutlinedInput-notchedOutline': {
    borderColor: '#555555',
  },
  '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
    borderColor: theme.palette.primary.main,
  },
  '& .MuiSvgIcon-root': {
    color: '#cccccc',
  },
  '& .MuiSelect-select': {
    backgroundColor: '#1e1e1e',
    color: '#cccccc',
    padding: '8px 12px',
    ...theme.typography.caption,
  },
}));

const StyledChipContainer = styled(Box)(() => ({
  display: 'flex',
  flexWrap: 'wrap',
  gap: '4px',
}));

const StyledChip = styled(Chip, {
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({
  backgroundColor: '#3c3c3c',
  color: '#cccccc',
  '&.MuiChip-root': {
    backgroundColor: '#3c3c3c',
    color: '#cccccc',
  },
  '& .MuiChip-label': {
    color: '#cccccc',
    ...theme.typography.caption,
  },
  '& .MuiChip-icon': {
    color: '#ffa500',
  },
  '& .MuiSvgIcon-root': {
    color: '#ffa500',
  },
}));

const StyledMenuItem = styled(MenuItem)(() => ({
  color: '#cccccc',
  backgroundColor: '#2d2d30',
  '&:hover': {
    backgroundColor: '#3c3c3c',
  },
  '&.Mui-selected': {
    backgroundColor: '#555555',
    '&:hover': {
      backgroundColor: '#666666',
    },
  },
}));

const StyledMenuItemContent = styled(Box)(() => ({
  display: 'flex',
  alignItems: 'center',
  gap: '8px',
}));

const StyledSectionBox = styled(Box)(() => ({
  backgroundColor: '#3c3c3c',
  border: '1px solid #555555',
  borderRadius: '4px',
  padding: '12px',
}));

const StyledSectionTitle = styled(Typography)(() => ({
  color: '#cccccc',
  marginBottom: '8px',
  display: 'block',
}));

const StyledSectionContent = styled(Typography)(() => ({
  color: '#888888',
}));