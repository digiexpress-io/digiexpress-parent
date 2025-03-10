import React from 'react';
import { useThemeProps, Button, SvgIconTypeMap, Typography, Stack } from '@mui/material';
import { OverridableComponent } from '@mui/material/OverridableComponent';
import PersonOutlinedIcon from '@mui/icons-material/PersonOutlined';
import LogoutIcon from '@mui/icons-material/Logout';
import { FormattedMessage } from 'react-intl';
import { EveliLoginRoot, EveliLogoutButton, MUI_NAME, useUtilityClasses } from './useUtilityClasses';

import { EveliOverridableComponent } from '../api-variants';
import { useConfig } from '../../frontdesk/context/ConfigContext';
import { useIam } from '../api-iam';



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
    <>
      <Root ownerState={ownerState} className={classes.root}>
        {user.authenticated ? 
        (<EveliLogoutButton className={classes.logout} variant="text" startIcon={<LogoutIcon />} onClick={handleOnClick}>
          <Stack spacing={0} alignItems="flex-start">
            <Typography><FormattedMessage id={'menu.logout'} /></Typography>
            <Typography variant="caption">{user.name}</Typography>
          </Stack>
        </EveliLogoutButton>) :
        (<Button type='submit' variant='contained' startIcon={<StartIcon />} onClick={handleOnClick}>
          <FormattedMessage id='explorer.login' />
        </Button>)}
      </Root>
    </>
  )
}