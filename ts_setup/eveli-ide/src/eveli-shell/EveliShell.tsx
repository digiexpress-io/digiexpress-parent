import React from 'react';

import { CssBaseline, useThemeProps, useTheme, useMediaQuery } from '@mui/material';
import { EveliShellProvider, useEveliShell } from './EveliShellContext';
import { EveliShellProps, EveliShellToolbarHeightOptions } from './EveliShellProps';
import { EveliShellRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';


const toolbarHeight: EveliShellToolbarHeightOptions = {
  xs: 64,
  sm: 64,
  md: 64,
  lg: 64,
  xl: 64,
};


function nonEmpty<T extends Record<string, any>>(any: T | undefined): T {
  return Object.entries(any ?? {})
    .filter(([, value]) => value !== undefined && value !== null)
    .reduce((collector, [key, value]) => {
      collector[key] = value;
      return collector;
    }, {} as any);
}

export const EveliShell: React.FC<EveliShellProps> = (initProps) => {
  const themeProps = useThemeProps({
    props: { ...initProps },
    name: MUI_NAME,
  });

  const ownerState = {
    footerHeight: 50,
    drawerWidth: 300,
    minibarWidth: 70,
    ...nonEmpty(themeProps),
    toolbarHeight: { ...toolbarHeight, ...nonEmpty(themeProps.toolbarHeight) },
  }
  return (
    <EveliShellProvider 
      drawerOpen={themeProps.drawerOpen ?? true} 
      drawerWidth={ownerState.drawerWidth} 
      footerHeight={ownerState.footerHeight}
      minibarWidth={ownerState.minibarWidth}
      toolbarHeight={ownerState.toolbarHeight}>
      
      <CssBaseline />
      <EveliShellInternal {...ownerState}>{themeProps.children}</EveliShellInternal>
    </EveliShellProvider>
  );
}


const EveliShellInternal: React.FC<EveliShellProps> = (initProps) => {
  const { open: drawerOpen, toolbarHeight: userToolbarHeight, drawerWidth, footerHeight } = useEveliShell();
  const toolbarHeight = useToolbarHeight(userToolbarHeight);

  const ownerState = {
    ...initProps,
    drawerOpen,
    drawerWidth,
    toolbarHeight,
    footerHeight,
  };

  const classes = useUtilityClasses();
  const Root = initProps.component ?? EveliShellRoot;
  return (
    <Root ownerState={ownerState as any} className={classes.root}>{initProps.children}</Root>
  );
}



const useToolbarHeight = (options: EveliShellToolbarHeightOptions): number => {
  const theme = useTheme();
  const xs = useMediaQuery(theme.breakpoints.only('xs'));
  const sm = useMediaQuery(theme.breakpoints.only('sm'));
  const md = useMediaQuery(theme.breakpoints.only('md'));
  const lg = useMediaQuery(theme.breakpoints.only('lg'));
  const xl = useMediaQuery(theme.breakpoints.only('xl'));

  let currentBreakpoint: keyof EveliShellToolbarHeightOptions = 'xs';
  if (xl) {
    currentBreakpoint = 'xl';
  } else if (lg) {
    currentBreakpoint = 'lg';
  } else if (md) {
    currentBreakpoint = 'md';
  } else if (sm) {
    currentBreakpoint = 'sm';
  } else if (xs) {
    currentBreakpoint = 'xs';
  }
  return options[currentBreakpoint] ?? 0;
}