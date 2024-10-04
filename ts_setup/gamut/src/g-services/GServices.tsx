import React from 'react';
import { Link, Typography, useThemeProps } from '@mui/material';
import { useUtilityClasses, MUI_NAME, GServicesRoot } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';

export interface GServicesProps {
  onClick: (event: React.MouseEvent) => void;
  children: React.ReactNode;
  component?: GOverridableComponent<GServicesProps>
}

export const GServices: React.FC<GServicesProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  })

  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props,
  }
  const Root = props.component ?? GServicesRoot;

  return (
    <Root ownerState={ownerState} className={classes.root}>
      <Link onClick={props.onClick} className={classes.serviceLink}>
        <Typography>{props.children}</Typography>
      </Link>
    </Root>
  )
}




