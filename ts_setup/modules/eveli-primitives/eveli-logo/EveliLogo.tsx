import React from 'react';
import { useThemeProps, useTheme, Box } from '@mui/material';
import { EveliLogoRoot, getVariant, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { EveliOverridableComponent } from '@dxs-ts/eveli-api';


export interface EveliLogoProps {
  variant?: 'black_lg';
  img?: any;
  onClick?: () => void;
  component?: EveliOverridableComponent<EveliLogoProps>;
}


export const EveliLogo: React.FC<EveliLogoProps> = (inProps) => {
  const theme = useTheme();
  const props = useThemeProps({ props: inProps, name: MUI_NAME });
  const ownerState = {
    variant: 'black_lg' as any,
    ...props
  };
  const variant = getVariant(theme, ownerState.variant);
  const classes = useUtilityClasses(props);

  const Root = props.component ?? EveliLogoRoot;
  return (
    <Box display='flex' justifyContent='center'>
      <Root 
        ownerState={ownerState} 
        src={variant?.props.img} 
        onClick={props.onClick} 
        className={classes.root} />
    </Box>);
}
