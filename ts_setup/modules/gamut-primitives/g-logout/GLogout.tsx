import React from 'react'
import { useThemeProps, Button, SvgIconTypeMap } from '@mui/material'
import { OverridableComponent } from '@mui/material/OverridableComponent'
import { PersonOutlined as PersonOutlinedIcon } from '@mui/icons-material'
import { FormattedMessage } from 'react-intl'

import { MUI_NAME, GLogoutRoot, useUtilityClasses } from './useUtilityClasses';
import { GAuthUn } from '../g-auth-un'
import { GOverridableComponent } from '@dxs-ts/gamut-api'



export interface GLogoutProps {
  icon?: OverridableComponent<SvgIconTypeMap> & { muiName: string };
  component?: GOverridableComponent<GLogoutProps>;
  hideStartIcon?: boolean;
}


export const GLogout: React.FC<GLogoutProps> = (initProps) => {
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

  return (
    <Root className={classes.root} ownerState={ownerState}>
      <GAuthUn>
        <Button 
          type='submit' 
          variant='outlined' 
          startIcon={!props.hideStartIcon ? <StartIcon /> : undefined}
        >
          <FormattedMessage id='gamut.buttons.logout' />
        </Button>
      </GAuthUn>
    </Root>
  )
}


















