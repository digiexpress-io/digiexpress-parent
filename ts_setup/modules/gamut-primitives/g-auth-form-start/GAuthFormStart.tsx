import { useThemeProps } from '@mui/material';
import React from 'react';
import { useUtilityClasses, MUI_NAME, GAuthRoot } from './useUtilityClasses';
import { GOverridableComponent } from '@dxs-ts/gamut-api';
import { IamForcedLogin, useIam } from '@dxs-ts/gamut-api';


export interface GAuthFormStartProps {
  children: React.ReactNode;
  component?: GOverridableComponent<GAuthFormStartProps>;

  action?: string;
  method?: string;
  onSubmit?: ((event: React.FormEvent<HTMLFormElement>) => void) | undefined;

  forced?: true; // force login after component load in new window
}


export const GAuthFormStart: React.FC<GAuthFormStartProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses();
  const { action, method = 'GET', onSubmit } = props;
  const Root = props.component ?? GAuthRoot;
  const { fetchUserGET, authType, reload } = useIam();

  const loginOn401 = React.useCallback(() => IamForcedLogin({
    loginUrl: action!, 
    user: fetchUserGET
  }),[action, fetchUserGET]);


  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    if(props.forced) {
      event.preventDefault();
      event.stopPropagation();
      if(authType !== 'ANON') {
        return;
      }
      if(props.forced !== true) {
        return
      }

      loginOn401().then(() => {
        reload().then((data) => {

          if (onSubmit && data) {
            onSubmit(event)
          }
        });
      });

    } else if(onSubmit) {
      onSubmit(event);
    }
  }

  return (
    <Root className={classes.root} ownerState={props}>
      <form action={action} method={method} onSubmit={handleSubmit}>
        {props.children}
      </form>
    </Root>
  )
}