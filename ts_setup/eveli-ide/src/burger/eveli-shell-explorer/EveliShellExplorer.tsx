import React from 'react';

import { useThemeProps } from '@mui/material';


import { EveliShellExplorerRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { EveliOverridableComponent } from '../api-variants' 
import { useEveliShell } from '../eveli-shell' 


export interface EveliShellExplorerProps {
  component?: EveliOverridableComponent<EveliShellExplorerProps>;
  children: React.ReactNode;
}

export const EveliShellExplorer: React.FC<EveliShellExplorerProps> = (initProps) => {
  const themeProps = useThemeProps({
    props: { ...initProps },
    name: MUI_NAME,
  });

  const { open: drawerOpen, drawerWidth, minibarWidth } = useEveliShell();

  const ownerState = {
    ...themeProps,
    drawerOpen,
    drawerWidth,
    minibarWidth
  };

  const classes = useUtilityClasses();
  const Root = initProps.component ?? EveliShellExplorerRoot;
  
  return (<Root ownerState={ownerState} className={classes.root}>{initProps.children}</Root>);
}

