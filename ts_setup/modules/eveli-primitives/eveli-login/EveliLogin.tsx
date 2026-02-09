import React from 'react';
import { useThemeProps, Button, SvgIconTypeMap, Typography, Stack } from '@mui/material';
import { OverridableComponent } from '@mui/material/OverridableComponent';
import { PersonOutlined as PersonOutlinedIcon } from '@mui/icons-material';
import { Logout as LogoutIcon } from '@mui/icons-material';
import { FormattedMessage } from 'react-intl';
import { EveliLoginRoot, EveliLogoutButton, MUI_NAME, useUtilityClasses } from './useUtilityClasses';

import { EveliOverridableComponent } from '@dxs-ts/eveli-api';

import { useIam, useConfig, EveliTenantFeatureEnabled } from '@dxs-ts/eveli-api';


export interface EveliLoginProps {
  icon?: OverridableComponent<SvgIconTypeMap> & { muiName: string }
  component?: EveliOverridableComponent<EveliLoginProps>;
}

export const EveliLogin: React.FC<EveliLoginProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }

  const config = useConfig();

  const { user } = useIam();
  const location = user.authenticated ? config.logoutUrl : config.loginUrl;

  const { icon: StartIcon = PersonOutlinedIcon } = props;
  const Root = props.component ?? EveliLoginRoot;

  function handleOnClick() {
    window.location.href = location
  }

  return (
    <EveliTenantFeatureEnabled id='LOGIN_BUTTON'>
      <Root ownerState={ownerState} className={classes.root}>
        {user.authenticated ? 
        (<EveliLogoutButton className={classes.logout} variant="text" startIcon={<LogoutIcon />} onClick={handleOnClick}>
          <Stack spacing={0} alignItems="flex-start">
            <Typography><FormattedMessage id={'menu.logout'} /></Typography>
            <Typography variant="caption">{user.name}</Typography>
          </Stack>
        </EveliLogoutButton>) :
        (<Button type='submit' variant='contained' startIcon={<StartIcon />} onClick={handleOnClick}>
          <FormattedMessage id='explorer.login' defaultMessage='Log in'/>
        </Button>)}
      </Root>
    </EveliTenantFeatureEnabled>
  )
}