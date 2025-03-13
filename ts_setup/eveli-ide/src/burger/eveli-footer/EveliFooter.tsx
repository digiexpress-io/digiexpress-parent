import React from 'react';
import { Typography, useThemeProps } from '@mui/material';
import { useUtilityClasses, MUI_NAME, EveliFooterRoot } from './useUtilityClasses';
import { EveliOverridableComponent } from '../api-variants';
import { FormattedMessage } from 'react-intl';
import { useFetch } from '@dxs-ts/eveli-fetch';


export interface EveliFooterProps {
  children?: React.ReactNode,
  component?: EveliOverridableComponent<EveliFooterProps>;
}


export const EveliFooter: React.FC<EveliFooterProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const info = useFetch('worker/rest/api/assets/stencil/version.GET', {});
  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }
  const Root = props.component ?? EveliFooterRoot

  return (
    <Root ownerState={ownerState} className={classes.root}>
      {props.children}            
      <Typography>
        <FormattedMessage id={"activities.version.composer"} values={{ version: info?.frontend.version, date: info?.frontend.built }} />
      </Typography>
      <Typography>
        <FormattedMessage id={"activities.version.core"} values={{ version: info?.backend.version, date: info?.backend.built }} />
      </Typography>
    </Root>
  )
}
