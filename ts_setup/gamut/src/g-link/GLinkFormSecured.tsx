import React from 'react';
import { Typography, styled, useThemeProps, generateUtilityClass, Link } from '@mui/material';
import LockPersonIcon from '@mui/icons-material/LockPerson';
import composeClasses from '@mui/utils/composeClasses';
import { GOverridableComponent } from '../g-override';




export interface GLinkFormSecuredClasses {
  root: string
}

export type GLinkFormSecuredClassKey = keyof GLinkFormSecuredClasses;

export interface GLinkFormSecuredProps {
  label: string;
  value: string;
  onClick: () => void;
  component?: GOverridableComponent<GLinkFormSecuredProps>;
}

const useUtilityClasses = (ownerState: GLinkFormSecuredProps) => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass('GLinkFormSecured', slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GLinkFormSecured: React.FC<GLinkFormSecuredProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: 'GLinkFormSecured',
  });
  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }
  const Root = props.component ?? GLinkFormSecuredRoot

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

const GLinkFormSecuredRoot = styled("div", {
  name: 'GLinkFormSecured',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
})<{ ownerState: GLinkFormSecuredProps }>(({ theme }) => {
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