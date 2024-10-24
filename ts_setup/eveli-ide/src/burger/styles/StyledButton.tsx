import React from 'react';
import { } from "@mui/material/styles";
import { Button, ButtonProps, Theme, styled, darken, alpha, SxProps } from '@mui/material';
import { FormattedMessage } from 'react-intl';



const StyledButtonRoot = styled(Button)<ButtonProps>(({ theme }) => ({
  borderRadius: theme.spacing(1),
  borderWidth: 0,
  fontWeight: 'bold',
  color: theme.palette.uiElements.main,
  '&:hover': {
    backgroundColor: alpha(theme.palette.uiElements.main, 0.1),
    border: 'none',
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



const StyledPrimaryButton: React.FC<{
  label: string,
  onClick: (event: React.MouseEvent<HTMLElement>) => void,
  sx?: SxProps<Theme>,
  disabled?: boolean
}> = (props) => {
  const title = <FormattedMessage id={props.label} />;
  return (
    <StyledButtonPrimaryRoot
      variant='contained'
      onClick={props.onClick}
      disabled={props.disabled}
      sx={props.sx}>
      {title}
    </StyledButtonPrimaryRoot>
  );
}

const StyledSecondaryButton: React.FC<{
  label?: string,
  onClick: (event: React.MouseEvent<HTMLElement>) => void,
  sx?: SxProps<Theme>,
  disabled?: boolean
}> = (props) => {
  const title = <FormattedMessage id={props.label} />;
  return (
    <StyledButtonRoot
      onClick={props.onClick}
      disabled={props.disabled}
      sx={props.sx}>{title}
    </StyledButtonRoot>
  );
}

export { StyledPrimaryButton, StyledSecondaryButton, StyledButtonRoot }