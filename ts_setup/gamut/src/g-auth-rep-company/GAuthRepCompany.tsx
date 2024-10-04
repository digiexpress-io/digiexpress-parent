import { useThemeProps } from '@mui/material';
import React from 'react';
import { useUtilityClasses, MUI_NAME, GAuthRepCompanyRoot } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';


export interface GAuthRepCompanyProps {
  children: React.ReactNode;
  component?: GOverridableComponent<GAuthRepCompanyProps>;

  action?: string;
  method?: string;
  onSubmit?: ((event: React.FormEvent<HTMLFormElement>) => void) | undefined;
}


export const GAuthRepCompany: React.ForwardRefExoticComponent<GAuthRepCompanyProps & React.RefAttributes<HTMLInputElement>> = React.forwardRef<HTMLInputElement, GAuthRepCompanyProps>((initProps, ref) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses();
  const { action, method = 'GET' } = props;
  const Root = props.component ?? GAuthRepCompanyRoot;

  return (
    <Root className={classes.root} ownerState={props}>
      <form action={action} method={method} onSubmit={props.onSubmit}>
        {props.children}
        <input hidden={true} type='submit' ref={ref}/>
      </form>
    </Root>
  )
})