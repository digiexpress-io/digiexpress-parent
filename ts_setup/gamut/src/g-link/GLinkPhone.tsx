import React from 'react';
import { useTheme, Typography, styled, useThemeProps, generateUtilityClass } from '@mui/material';
import LocalPhoneIcon from '@mui/icons-material/LocalPhone';
import composeClasses from '@mui/utils/composeClasses';
import { GOverridableComponent } from '../g-override';



export interface GLinkPhoneClasses {
  root: string;
}
export type GLinkPhoneClassKey = keyof GLinkPhoneClasses;

export interface GLinkPhoneProps {
  label: string;
  value: string;
  component?: GOverridableComponent<GLinkPhoneProps>;
}

const useUtilityClasses = (ownerState: GLinkPhoneProps) => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass('GLinkPhone', slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GLinkPhone: React.FC<GLinkPhoneProps> = (initProps) => {
  const theme = useTheme();
  const props = useThemeProps({
    props: initProps,
    name: 'GLinkPhone',
  });
  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }
  const Root = props.component ?? GLinkPhoneRoot
  
  return (
    <Root className={classes.root} ownerState={ownerState}>
      <Typography>{props.label}</Typography>
      <span>
        <LocalPhoneIcon color='info' sx={{ mr: theme.spacing(1) }} />
        <Typography>{props.value}</Typography>
      </span>
    </Root>)
}

const GLinkPhoneRoot = styled("div", {
  name: 'GLinkPhone',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
})<{ ownerState: GLinkPhoneProps }>(({ theme }) => {
  return {
    "span": {
      display: 'flex',
      alignItems: 'center'
    },
    "& .MuiSvgIcon-root": {
      marginRight: theme.spacing(1),
      fontSize: '20px'
    }
  };
});