import React from 'react';
import { Link, styled, useThemeProps } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import generateUtilityClass from '@mui/utils/generateUtilityClass';

import OpenInNewIcon from '@mui/icons-material/OpenInNew';
import { GOverridableComponent } from '../g-override';

export interface GLinkHyperClasses {
  root: string;
}
export type GLinkHyperClassKey = keyof GLinkHyperClasses;

export interface GLinkHyperProps {
  label: string;
  value: string;
  onClick?: () => void;
  component?: GOverridableComponent<GLinkHyperProps>;
}

const useUtilityClasses = (ownerState: GLinkHyperProps) => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass('GLinkHyper', slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const GLinkHyper: React.FC<GLinkHyperProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: 'GLinkHyper',
  });
  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }

  const Root = props.component ?? GLinkHyperRoot

  return (
    <Root className={classes.root} ownerState={ownerState}>
      <Link href={props.value} target='_blank'>
        <span>
          {props.label}
          <OpenInNewIcon />
        </span>
      </Link>
    </Root>
  )
}


const GLinkHyperRoot = styled("div", {
  name: 'GLinkHyper',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
})<{ ownerState: GLinkHyperProps }>(({ theme }) => {
  return {
    "span": {
      wordBreak: 'break-word'
    },
    "& .MuiSvgIcon-root": {
      verticalAlign: 'sub',
      marginLeft: theme.spacing(0.5),
      color: theme.palette.text.disabled,
      fontSize: '20px'
    }
  };
});