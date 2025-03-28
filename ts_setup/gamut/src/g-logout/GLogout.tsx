import React from 'react'
import { useThemeProps, Button, SvgIconTypeMap } from '@mui/material'
import { OverridableComponent } from '@mui/material/OverridableComponent'
import PersonOutlinedIcon from '@mui/icons-material/PersonOutlined'
import { FormattedMessage } from 'react-intl'

import { MUI_NAME, GLogoutRoot, useUtilityClasses } from './useUtilityClasses';
import { GAuthUn } from '../g-auth-un'
import { GOverridableComponent } from '../g-override'
import { useIam } from '../api-iam'



export interface GLogoutProps {
  icon?: OverridableComponent<SvgIconTypeMap> & { muiName: string };
  component?: GOverridableComponent<GLogoutProps>;
  showIdentity?: boolean;
}


export const GLogout: React.FC<GLogoutProps> = (initProps) => {
  const { showIdentity, ...restProps } = initProps;

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses(props);

  const { icon: StartIcon = PersonOutlinedIcon } = props;
  const ownerState = {
    ...props
  }
  const Root = props.component ?? GLogoutRoot;
  const {userName} = useIam();

  return (
    <Root className={classes.root} ownerState={ownerState}>
      <GAuthUn>
        <div className={classes.logoutLayout}>
          {showIdentity && (
            <div className={classes.userIdentityLabel}>
              <div className={classes.userIdentityText}>
                <FormattedMessage id="header.userIdentity.label" />
              </div>
              <div className={classes.userDisplayName}>
                {userName}
              </div>
            </div>
          )}
          <Button type='submit' variant='outlined' startIcon={<StartIcon />}>
            <FormattedMessage id='gamut.buttons.logout' />
          </Button>
        </div>
      </GAuthUn>
    </Root>
  )
}


















