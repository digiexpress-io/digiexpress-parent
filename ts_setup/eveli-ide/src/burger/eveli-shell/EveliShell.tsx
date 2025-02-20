import React from 'react';

import { CssBaseline, useThemeProps, useTheme, useMediaQuery } from '@mui/material';
import { EveliShellProvider, useEveliShell } from './EveliShellContext';
import { EveliShellProps, EveliShellToolbarHeightOptions } from './EveliShellProps';
import { EveliShellRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';




export const EveliShell: React.FC<EveliShellProps> = (initProps) => {
  const themeProps = useThemeProps({
    props: { ...initProps },
    name: MUI_NAME,
  });

  return (
    <EveliShellProvider drawerOpen={themeProps.drawerOpen ?? true}>
      <CssBaseline />
      <EveliShellInternal {...themeProps}>{themeProps.children}</EveliShellInternal>
    </EveliShellProvider>
  );
}


const EveliShellInternal: React.FC<EveliShellProps> = (initProps) => {
  const toolbarOptions: EveliShellToolbarHeightOptions = {
    xs: 64,
    sm: 64,
    md: 64,
    lg: 64,
    xl: 64,
    ...(initProps.toolbarHeight ?? {})
  };

  const toolbarHeight = useToolbarHeight(toolbarOptions);
  const { open: drawerOpen } = useEveliShell();

  const ownerState = {
    footerHeight: 50,
    drawerWidth: 300,
    ...initProps,
    drawerOpen,
    toolbarHeight,
  };

  const classes = useUtilityClasses();
  const Root = initProps.component ?? EveliShellRoot;
  return (
    <Root ownerState={ownerState} className={classes.root}>{initProps.children}</Root>
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