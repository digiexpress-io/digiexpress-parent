import React from 'react';
import { Typography, styled, useThemeProps, generateUtilityClass, Link } from '@mui/material';
import LockPersonIcon from '@mui/icons-material/LockPerson';
import composeClasses from '@mui/utils/composeClasses';
import { GOverridableComponent } from '@dxs-ts/gamut-api';


const MUI_NAME = 'GLinkFormLocked';


export interface GLinkFormLockedClasses {
  root: string
}

export type GLinkFormLockedClassKey = keyof GLinkFormLockedClasses;

export interface GLinkFormLockedProps {
  label: string;
  value: string;
  onClick: () => void;
  component?: GOverridableComponent<GLinkFormLockedProps>;
}

const useUtilityClasses = (ownerState: GLinkFormLockedProps) => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GLinkFormLocked: React.FC<GLinkFormLockedProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }
  const Root = props.component ?? GLinkFormLockedRoot

  return (
    <Root ownerState={ownerState} className={classes.root} onClick={props.onClick}>
      <Link>
        <span>
          <LockPersonIcon color='error' />
          <Typography>{props.label}</Typography>
        </span>
      </Link>
    </Root>
  )
}

const GLinkFormLockedRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
})<{ ownerState: GLinkFormLockedProps }>(({ theme }) => {
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