import React from 'react';
import { CircularProgress, Typography, useThemeProps } from '@mui/material';
import { useIntl } from 'react-intl';
import { GLoaderRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';


export interface GLoaderProps {
  component?: GOverridableComponent<GLoaderProps>;
}

export const GLoader: React.FC<GLoaderProps> = (initProps) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const ownerState = {
    ...props,
  }

  const Root = props.component ?? GLoaderRoot;
  return (
    <Root className={classes.root} ownerState={ownerState}>
      <>
        <CircularProgress />
        <Typography>{intl.formatMessage({ id: 'gamut.loading' })}</Typography>
      </>
    </Root>
  )
}
