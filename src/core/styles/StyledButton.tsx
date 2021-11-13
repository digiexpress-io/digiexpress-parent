import React from 'react';
import { styled, darken, alpha } from "@mui/material/styles";
import { SxProps } from '@mui/system';
import { Button, ButtonProps, Theme } from '@mui/material';
import { FormattedMessage } from 'react-intl';



const StyledButtonRoot = styled(Button)<ButtonProps>(({ theme }) => ({
  borderRadius: theme.spacing(1),
  fontWeight: 'bold',
  color: theme.palette.uiElements.main,
  '&:hover': {
    backgroundColor: alpha(theme.palette.uiElements.main, 0.1),
  },
}));


const StyledButtonPrimaryRoot = styled(Button)<ButtonProps>(({ theme }) => ({
  borderRadius: theme.spacing(1),
  fontWeight: 'bold',
  backgroundColor: theme.palette.uiElements.main,

  '&:hover': {
    backgroundColor: darken(theme.palette.uiElements.main, 0.2),
  },
}));



const StyledPrimaryButton: React.FC<{ label: string, onClick: () => void, sx?: SxProps<Theme>  }> = (props) => {
  const title = <FormattedMessage id={props.label} />;
  return (
    <StyledButtonPrimaryRoot
      variant='contained'
      onClick={props.onClick}
      sx={props.sx}>{title}</StyledButtonPrimaryRoot>
  );
}

const StyledSecondaryButton: React.FC<{ label: string, onClick: () => void, sx?: SxProps<Theme> }> = (props) => {
  const title = <FormattedMessage id={props.label} />;
  return (
    <StyledButtonRoot
      variant='text'
      onClick={props.onClick}
      sx={props.sx}>{title}</StyledButtonRoot>
  );
}

export { StyledPrimaryButton, StyledSecondaryButton, StyledButtonRoot }