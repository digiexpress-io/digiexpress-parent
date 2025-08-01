import React from 'react';
import { useThemeProps } from '@mui/material';
import { useUtilityClasses, MUI_NAME, GFooterRoot } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';




export interface GFooterProps {
  children?: React.ReactNode,
  component?: GOverridableComponent<GFooterProps>;
}


export const GFooter: React.FC<GFooterProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }
  const Root = props.component ?? GFooterRoot

  return (
    <Root ownerState={ownerState} className={classes.root}>
      {props.children}
    </Root>
  )
}
