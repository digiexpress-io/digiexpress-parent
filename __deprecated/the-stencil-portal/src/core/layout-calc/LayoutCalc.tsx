import React from 'react';

import { CssBaseline, Box, useMediaQuery, useTheme } from '@mui/material';
import { SxProps } from '@mui/system';

import { BreakpointConfig } from '../context/AppAPI';
import { useDrawer } from '../context/drawer/DrawerContext';


interface LayoutCalcCallbackProps {
  secondary: SxProps, 
  main: SxProps,
  drawerWidth: string | number  
}

interface LayoutCalcProps {
  config: BreakpointConfig,
  children: (props: LayoutCalcCallbackProps) => React.ReactNode
}

const MAX_SPACE = { display: 'flex', height: "100vh" };

const LayoutCalc: React.FC<LayoutCalcProps> = ({ config, children }) => {

  const layout = useDrawer();
  const drawerOpen = layout.session.drawer;
  const theme = useTheme();
  const { drawerWidth: drawerWidthCallback, main: mainCallback, toolbarHeight, secondary: secondaryCallback } = config;


  // style calcs  
  const drawerWidth = React.useMemo(() => drawerWidthCallback(theme, useMediaQuery), [theme, drawerWidthCallback]);
  const main: SxProps = React.useMemo(() => {
    if (drawerOpen) {
      return Object.assign({ 
        flexGrow: 1, 
        height: `calc(100vh - ${toolbarHeight}px)`, 
        width: `calc(100vw - ${typeof drawerWidth === 'number' ? drawerWidth + 'px' : drawerWidth})` 
      }, mainCallback({drawerOpen}));
    }
    return Object.assign({ flexGrow: 1, height: `calc(100vh - ${toolbarHeight}px)` }, mainCallback({drawerOpen}));
  }, [drawerOpen, toolbarHeight, drawerWidth, mainCallback])

  const secondary: SxProps = React.useMemo(
    () => Object.assign({ width: drawerWidth, height: `calc(100vh - ${toolbarHeight}px)` }, secondaryCallback({drawerOpen})), 
    [drawerWidth, secondaryCallback, drawerOpen, toolbarHeight]);
  
  const contents = React.useMemo(() => children({ main, secondary, drawerWidth }), [main, secondary, drawerWidth, children]);

  return (<Box sx={MAX_SPACE}><CssBaseline />{contents}</Box>);
}

export { LayoutCalc, LayoutCalcProps, LayoutCalcCallbackProps };
