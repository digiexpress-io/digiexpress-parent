import React from 'react';
import { Link, Typography, useThemeProps } from '@mui/material';
import { useUtilityClasses, MUI_NAME, GSecuredServicesRoot } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';

export interface GSecuredServicesProps {
  onClick: (event: React.MouseEvent) => void;
  children: React.ReactNode;
  component?: GOverridableComponent<GSecuredServicesProps>
}

export const GSecuredServices: React.FC<GSecuredServicesProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  })

  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props,
  }
  const Root = props.component ?? GSecuredServicesRoot;

  return (
    <Root ownerState={ownerState} className={classes.root}>
      <Link onClick={props.onClick} className={classes.serviceLink}>
        <Typography>{props.children}</Typography>
      </Link>
    </Root>
  )
}




