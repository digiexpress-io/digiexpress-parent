import React from 'react';
import { Typography, styled, useThemeProps, generateUtilityClass, Link } from '@mui/material';
import ForwardIcon from '@mui/icons-material/Forward';
import composeClasses from '@mui/utils/composeClasses';
import { GOverridableComponent } from '../g-override';


export interface GLinkFormUnsecuredClasses {
  root: string
}

export type GLinkFormUnsecuredClassKey = keyof GLinkFormUnsecuredClasses;

export interface GLinkFormUnsecuredProps {
  label: string;
  value: string;
  onClick: () => void;
  component?: GOverridableComponent<GLinkFormUnsecuredProps>;
}

const useUtilityClasses = (ownerState: GLinkFormUnsecuredProps) => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass('GLinkFormUnsecured', slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GLinkFormUnsecured: React.FC<GLinkFormUnsecuredProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: 'GLinkFormUnsecured',
  });
  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }
  const Root = props.component ?? GLinkFormUnsecuredRoot

  return (
    <Root ownerState={ownerState} className={classes.root} onClick={props.onClick}>
      <Link>
        <span>
          <ForwardIcon color='info' />
          <Typography>{props.label}</Typography>
        </span>
      </Link>
    </Root>
  )
}

const GLinkFormUnsecuredRoot = styled("div", {
  name: 'GLinkFormUnsecured',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
})<{ ownerState: GLinkFormUnsecuredProps }>(({ theme }) => {
  return {
    "span": {
      display: 'flex',
      alignItems: 'center'
    },
    "& .MuiSvgIcon-root": {
      marginRight: theme.spacing(1),
      fontSize: '20px'
    }
  }
});