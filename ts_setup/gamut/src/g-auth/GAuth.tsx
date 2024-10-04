import { useThemeProps } from '@mui/material';
import React from 'react';
import { useUtilityClasses, MUI_NAME, GAuthRoot } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';


export interface GAuthProps {
  children: React.ReactNode;
  component?: GOverridableComponent<GAuthProps>;

  action?: string;
  method?: string;
  onSubmit?: ((event: React.FormEvent<HTMLFormElement>) => void) | undefined; 
}


export const GAuth: React.FC<GAuthProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses();
  const { action, method = 'GET', onSubmit } = props;
  const Root = props.component ?? GAuthRoot;

  return (
    <Root className={classes.root} ownerState={props}>
      <form action={action} method={method} onSubmit={onSubmit}>
        {props.children}
      </form>
    </Root>
  )
}