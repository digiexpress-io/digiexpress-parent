import React from 'react';
import { useThemeProps, Button, SvgIconTypeMap } from '@mui/material';
import { OverridableComponent } from '@mui/material/OverridableComponent';
import PersonOutlinedIcon from '@mui/icons-material/PersonOutlined';
import { FormattedMessage } from 'react-intl';
import { EveliLoginRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';

import { EveliOverridableComponent } from '../api-variants';
import { useConfig } from '../../frontdesk/context/ConfigContext';
import { useIam } from '../';



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
  const label = user.authenticated ? 'explorer.logout' : 'explorer.login';
  const location = user.authenticated ? config.logoutUrl : config.loginUrl;

  const { icon: StartIcon = PersonOutlinedIcon } = props;
  const Root = props.component ?? EveliLoginRoot;

  return (
    <>
      <Root ownerState={ownerState} className={classes.root}>
        <Button type='submit' variant='contained' startIcon={<StartIcon />} onClick={() => window.location.href = location}>
          <FormattedMessage id={label} />
        </Button>
      </Root>
    </>
  )
}

















