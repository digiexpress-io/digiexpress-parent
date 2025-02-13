import React from 'react';
import { styled, darken, alpha } from "@mui/material/styles";
import { SxProps } from '@mui/system';
import { Button, ButtonProps, Theme } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { cyan } from 'components-colors';



const StyledButtonRoot = styled(Button)<ButtonProps>(({ theme }) => ({
  borderRadius: theme.spacing(1),
  borderWidth: 0,
  fontWeight: 'bold',
  color: cyan,
  '&:hover': {
    backgroundColor: alpha(cyan, 0.1),
    border: 'none',
  },
}));


const StyledButtonPrimaryRoot = styled(Button)<ButtonProps>(({ theme }) => ({
  borderRadius: theme.spacing(1),
  fontWeight: 'bold',
  backgroundColor: cyan,
  '&:hover': {
    backgroundColor: darken(cyan, 0.2),
  },
}));



const StyledPrimaryButton: React.FC<{
  label: string | React.ReactNode,
  onClick: (event: React.MouseEvent<HTMLElement>) => void,
  sx?: SxProps<Theme>,
  disabled?: boolean
}> = (props) => {
  const title =  typeof props.label === 'string' ? <FormattedMessage id={props.label as string} /> : props.label;

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
  label?: string | React.ReactNode,
  labelValues?: Record<string, React.ReactNode>;
  onClick: (event: React.MouseEvent<HTMLElement>) => void,
  sx?: SxProps<Theme>,
  disabled?: boolean
}> = (props) => {
  const title =  typeof props.label === 'string' ? <FormattedMessage id={props.label as string} values={props.labelValues}/> : props.label;
  return (
    <StyledButtonRoot
      onClick={props.onClick}
      disabled={props.disabled}
      sx={props.sx}>{title}
    </StyledButtonRoot>
  );
}

export { StyledPrimaryButton, StyledSecondaryButton, StyledButtonRoot }