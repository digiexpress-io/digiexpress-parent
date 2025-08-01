import { useThemeProps } from '@mui/material';
import React from 'react';
import { useUtilityClasses, MUI_NAME, GAuthUnRepCompanyRoot } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';


export interface GAuthUnRepCompanyProps {
  children: React.ReactNode;
  component?: GOverridableComponent<GAuthUnRepCompanyProps>;

  action?: string;
  method?: string;
  onSubmit?: ((event: React.FormEvent<HTMLFormElement>) => void) | undefined;
}


export const GAuthUnRepCompany: React.FC<GAuthUnRepCompanyProps & React.RefAttributes<HTMLInputElement>> = React.forwardRef<HTMLInputElement, GAuthUnRepCompanyProps>((initProps, ref) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses();
  const { action, method = 'GET', component } = props;
  const Root = component ?? GAuthUnRepCompanyRoot;

  return (
    <Root className={classes.root} ownerState={props} >
      <form action={action} method={method} onSubmit={props.onSubmit}>
        {props.children}
        <input hidden={true} type='submit' ref={ref}/>
      </form>
    </Root>
  )
})
