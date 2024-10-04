import React from 'react';
import { useThemeProps, useTheme } from '@mui/material';
import { GLogoRoot, getVariant, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';


export interface GLogoProps {
  variant?: 'white_lg' | 'white_sm' | 'black_lg' | 'black_sm' | 'black_sm_mob';
  img?: any;
  onClick?: () => void;
  component?: GOverridableComponent<GLogoProps>;
}


export const GLogo: React.FC<GLogoProps> = (inProps) => {
  const theme = useTheme();
  const props = useThemeProps({ props: inProps, name: MUI_NAME });
  const ownerState = {
    ...props
  };
  const variant = getVariant(theme, ownerState.variant);
  const classes = useUtilityClasses(props);

  const Root = props.component ?? GLogoRoot;

  return (<Root 
    ownerState={ownerState} 
    src={variant?.props.img} 
    onClick={props.onClick} 
    className={classes.root} />);
}





