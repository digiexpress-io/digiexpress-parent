import React from 'react';
import { Box, TextField, Typography, FormControl, Select, MenuItem, Chip, OutlinedInput, styled, Button } from '@mui/material';
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
        <Typography variant='caption'>Name</Typography>
        <StyledTextField placeholder='Asset name' size='small' fullWidth />

        <Typography variant='caption'>3-digit order number</Typography>
        <StyledTextField placeholder="100" size='small' fullWidth />

        <Typography variant='caption'>Description</Typography>
        <StyledTextField placeholder="Description" size='small' fullWidth multiline minRows={2} maxRows={5} />

        <Typography variant='caption'>Config options</Typography>
        <StyledFormControl fullWidth size='small'>
          <StyledSelect multiple value={mockSelectedValues} input={<OutlinedInput />}
            renderValue={(selected) => (
              <StyledChipContainer>
                {(selected as string[]).map((value) => {
                  const option = configOptions.find(opt => opt.value === value);
                  return (
                    <StyledChip key={value} icon={option?.icon} label={option?.label} size="small" variant='outlined' />
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

        <Typography variant='caption'>Labels</Typography>
        <StyledTextField size='small' fullWidth placeholder="Select or add new labels" />

        <Typography variant='caption'>Comments</Typography>
        <StyledTextField placeholder="Notes about this asset" size='small' fullWidth multiline minRows={2} maxRows={5} />

        <StyledSectionTitle variant='caption'>Sharing and Permissions</StyledSectionTitle>
        <StyledSectionBox>
          <StyledSectionContent variant='body2'>Put content here</StyledSectionContent>
        </StyledSectionBox>
        <StyledButtonContainer>
          <StyledCancelButton>Cancel</StyledCancelButton>
          <StyledSaveButton>Save</StyledSaveButton>
        </StyledButtonContainer>
      </StyledFormContainer>
    </ >
  );
};

const StyledCaption = styled(Typography)(() => ({
  color: '#cccccc',
}));

const StyledFormContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  gap: theme.spacing(0.5),
  padding: '8px 0',
}));

const StyledTextField = styled(TextField)(({ theme }) => ({
  width: '100%',
  marginTop: '0px',
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
      border: `1px solid #cccccc`
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
  '& .MuiInputBase-input.MuiOutlinedInput-input.MuiInputBase-inputMultiline': {
    padding: 'unset',
  },
  '& .MuiInputLabel-root': {
    color: '#cccccc',
    ...theme.typography.caption,
  },
}));

const StyledFormControl = styled(FormControl)(({ theme }) => ({
  width: '100%',
  marginTop: 'unset !important',
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
    border: `1px solid #cccccc`
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
  marginTop: '0px !important'
}));

const StyledSectionTitle = styled(Typography)(() => ({
  color: '#cccccc',
  marginBottom: '8px',
  display: 'block',
}));

const StyledSectionContent = styled(Typography)(() => ({
  color: '#888888',
}));

const StyledButtonContainer = styled(Box)(() => ({
  display: 'flex',
  gap: '12px',
  marginTop: '16px',
  justifyContent: 'flex-end',
}));

const StyledCancelButton = styled(Button)(() => ({
  backgroundColor: 'rgba(255, 0, 0, 0.1)',
  color: '#ff4444',
  border: '1px solid rgba(255, 0, 0, 0.3)',
  borderRadius: '4px',
  padding: '6px 16px',
  textTransform: 'none',
  fontSize: '12px',
  '&:hover': {
    backgroundColor: 'rgba(255, 0, 0, 0.2)',
  },
}));

const StyledSaveButton = styled(Button)(() => ({
  backgroundColor: '#404040',
  color: '#ffffff',
  border: '1px solid #777777',
  borderRadius: '4px',
  padding: '6px 16px',
  textTransform: 'none',
  fontSize: '12px',
  fontWeight: 500,
  '&:hover': {
    backgroundColor: '#505050',
    borderColor: '#999999',
  },
}));