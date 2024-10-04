import React from 'react';

import { CssBaseline, useThemeProps, useTheme, useMediaQuery } from '@mui/material';
import { GShellProvider, useGShell } from './GShellContext';
import { GShellProps, GShellToolbarHeightOptions } from './GShellProps';
import { GShellRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';




export const GShell: React.FC<GShellProps> = (initProps) => {
  const themeProps = useThemeProps({
    props: { ...initProps },
    name: MUI_NAME,
  });

  return (
    <GShellProvider drawerOpen={themeProps.drawerOpen ?? true}>
      <CssBaseline />
      <GShellInternal {...themeProps}>{themeProps.children}</GShellInternal>
    </GShellProvider>
  );
}


const GShellInternal: React.FC<GShellProps> = (initProps) => {
  const toolbarOptions: GShellToolbarHeightOptions = {
    xs: 150,
    sm: 150,
    md: 90,
    lg: 90,
    xl: 90,
    ...(initProps.toolbarHeight ?? {})
  };

  const toolbarHeight = useToolbarHeight(toolbarOptions);
  const { open: drawerOpen } = useGShell();

  const ownerState = {
    footerHeight: 300,
    drawerWidth: 300,
    ...initProps,

    drawerOpen,
    toolbarHeight,
  };

  const classes = useUtilityClasses();
  const Root = initProps.component ?? GShellRoot;
  return (
    <Root ownerState={ownerState} className={classes.root}>{initProps.children}</Root>
  );
}



const useToolbarHeight = (options: GShellToolbarHeightOptions): number => {
  const theme = useTheme();
  const xs = useMediaQuery(theme.breakpoints.only('xs'));
  const sm = useMediaQuery(theme.breakpoints.only('sm'));
  const md = useMediaQuery(theme.breakpoints.only('md'));
  const lg = useMediaQuery(theme.breakpoints.only('lg'));
  const xl = useMediaQuery(theme.breakpoints.only('xl'));

  let currentBreakpoint: keyof GShellToolbarHeightOptions = 'xs';
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