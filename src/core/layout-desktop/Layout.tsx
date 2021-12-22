import React from 'react';

import { CssBaseline, Toolbar, Typography, Box } from '@mui/material';
import { SxProps } from '@mui/system';
import { styled } from "@mui/material/styles";

import StyledToolbar from './Toolbar';
import StyledAppBar from './Appbar';
import StyledDrawer from './Drawer';
import Tabs from './Tabs';
import { useDrawer } from '../context/drawer/DrawerContext';

interface ContainerProps {
  main: React.ReactElement;
  secondary: React.ReactElement;
  toolbar: React.ReactElement;
  config: {
    drawerWidth: number,
    toolbarHeight: number,
  }
};

const drawerWidth = { expanded: 500 };

const toolbarStyle: SxProps = {
  backgroundColor: "primary.main",
  color: "primary.contrastText",
  overflow: "hidden",
  position: "fixed",
  height: "100%"
};
const secondaryStyle: SxProps = {
  width: drawerWidth.expanded,
  //marginLeft: `${drawerWidth.collapsed + 1}px`, 
  height: "100%"
};
const mainStyle: (drawerOpen: boolean) => SxProps = (drawerOpen) => (drawerOpen ?
  { flexGrow: 1, overflow: "auto", height: "calc(100vh - 64px)", width: `calc(100vw - ${drawerWidth}px)` } :
  { flexGrow: 1, overflow: "auto", marginLeft: '60px', height: "calc(100vh - 64px)" });

const drawerStyle: SxProps = { 
  display: 'flex', 
  overflowY: "scroll", 
  height: "100vh", 
  backgroundColor: 'primary.main', 
  color: 'primary.contrastText', 
  pl: 2 };


const StyledMain = styled("main")(() => ({
  width: "100%",
  height: "100%"
}));


const Container: React.FC<ContainerProps> = (components) => {
  const layout = useDrawer();
  const drawerOpen = layout.session.drawer;
  const { main, secondary, toolbar } = components;

  const mainWindow = React.useMemo(() => main, [main]);
  const secondaryWindow = React.useMemo(() => secondary, [secondary]);
  const toolbarWindow = React.useMemo(() => toolbar, [toolbar]);

  return (<Box sx={{ display: 'flex', height: "100vh" }}>
    <CssBaseline />
    <StyledAppBar position="fixed">
      <StyledToolbar disableGutters toolbarHeight={components.config.toolbarHeight} sx={{ alignItems: "self-start" }}>
        {toolbarWindow}
      </StyledToolbar>
    </StyledAppBar>

    <StyledDrawer variant="permanent" open={drawerOpen} drawerWidth={components.config.drawerWidth}>
      <StyledToolbar disableGutters toolbarHeight={components.config.toolbarHeight}  />
      <Box sx={drawerStyle}>
        {drawerOpen ? (<Box sx={secondaryStyle}>{secondaryWindow}</Box>) : null}
      </Box>
    </StyledDrawer>

    <StyledMain>
      <StyledToolbar disableGutters toolbarHeight={components.config.toolbarHeight} />
      <Box sx={mainStyle(drawerOpen)}>{mainWindow}</Box>
    </StyledMain>
  </Box>);
}

export type { ContainerProps };
export { Container };
