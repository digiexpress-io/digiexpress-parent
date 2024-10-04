import { useThemeProps } from '@mui/material';
import React from 'react';
import { useUtilityClasses, MUI_NAME, GAuthRepPersonRoot } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';


export interface GAuthRepPersonProps {
  children: React.ReactNode;
  component?: GOverridableComponent<GAuthRepPersonProps>;

  action?: string;
  method?: string;
  onSubmit?: (() => void) | undefined;
}


export const GAuthRepPerson: React.FC<GAuthRepPersonProps & React.RefAttributes<HTMLInputElement>> = React.forwardRef<HTMLInputElement, GAuthRepPersonProps>((initProps, ref) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses();
  const { action, method = 'GET' } = props;
  const Root = props.component ?? GAuthRepPersonRoot;

  return (
    <Root className={classes.root} ownerState={props}>
      <form action={action} method={method} onSubmit={props.onSubmit}>
        {props.children}
        <input hidden={true} type='submit' ref={ref}/>
      </form>
    </Root>
  )
})

