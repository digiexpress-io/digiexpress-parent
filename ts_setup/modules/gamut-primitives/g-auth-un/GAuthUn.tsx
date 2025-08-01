import { useThemeProps } from '@mui/material';
import React from 'react';
import { useUtilityClasses, MUI_NAME, GAuthUnRoot } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';


export interface GAuthUnProps {
  children: React.ReactNode;
  component?: GOverridableComponent<GAuthUnProps>;

  action?: string;
  method?: string;
  onSubmit?: ((event: React.FormEvent<HTMLFormElement>) => void) | undefined;
}


export const GAuthUn: React.FC<GAuthUnProps & React.RefAttributes<HTMLInputElement>> = React.forwardRef<HTMLInputElement, GAuthUnProps>((initProps, ref) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses();
  const { action, method = 'POST' } = props;
  const Root = props.component ?? GAuthUnRoot;

  return (
    <Root className={classes.root} ownerState={props}>
      <form action={action} method={method} onSubmit={props.onSubmit}>
        {props.children}
        <input hidden={true} type='submit' ref={ref}/>
      </form>
    </Root>
  )
})