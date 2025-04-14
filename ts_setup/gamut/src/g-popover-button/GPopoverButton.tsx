import React from 'react';
import { IconButton, useThemeProps } from '@mui/material';
import { GPopoverButtonRoot, useUtilityClasses, MUI_NAME, PopoverButton } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';


export interface GPopoverButtonProps {
  label: React.ReactNode;
  iconRotated?: boolean;
  icon?: React.ReactElement;
  onClick: (event: React.MouseEvent<HTMLButtonElement>) => void;
  component?: GOverridableComponent<GPopoverButtonProps>;
}


export const GPopoverButton: React.FC<GPopoverButtonProps> = (initProps) => {
  const classes = useUtilityClasses();
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME
  });

  const Root = props.component ?? GPopoverButtonRoot;
  return (

    <Root className={classes.root} ownerState={props}>
      <PopoverButton onClick={props.onClick} className={classes.button} ownerState={props}>
        {props.label}
        {props.icon && <IconButton className={classes.iconButton}>{props.icon}</IconButton>}
      </PopoverButton>
    </Root>
  )
}


