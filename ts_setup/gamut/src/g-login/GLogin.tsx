import React from 'react';
import { useThemeProps, Button, SvgIconTypeMap } from '@mui/material';
import { OverridableComponent } from '@mui/material/OverridableComponent';
import PersonOutlinedIcon from '@mui/icons-material/PersonOutlined';
import { FormattedMessage } from 'react-intl';
import { GLoginRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { GAuth } from '../g-auth';
import { GOverridableComponent } from '../g-override';


export interface GLoginProps {
  icon?: OverridableComponent<SvgIconTypeMap> & { muiName: string }
  component?: GOverridableComponent<GLoginProps>;
}

export const GLogin: React.FC<GLoginProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }

  const { icon: StartIcon = PersonOutlinedIcon } = props;
  const Root = props.component ?? GLoginRoot;


  return (
    <GAuth>
      <Root ownerState={ownerState} className={classes.root}>
        <Button type='submit' variant='contained' startIcon={<StartIcon />}>
          <FormattedMessage id='gamut.buttons.login' />
        </Button>
      </Root>
    </GAuth>
  )
}

















