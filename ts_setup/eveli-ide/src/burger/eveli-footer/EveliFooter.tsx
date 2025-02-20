import React from 'react';
import { useThemeProps } from '@mui/material';
import { useUtilityClasses, MUI_NAME, EveliFooterRoot } from './useUtilityClasses';
import { EveliOverridableComponent } from '../api-variants';




export interface EveliFooterProps {
  children?: React.ReactNode,
  component?: EveliOverridableComponent<EveliFooterProps>;
}


export const EveliFooter: React.FC<EveliFooterProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }
  const Root = props.component ?? EveliFooterRoot

  return (
    <Root ownerState={ownerState} className={classes.root}>
      {props.children}
    </Root>
  )
}
