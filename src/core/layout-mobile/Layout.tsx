import React from 'react';

import { CssBaseline, Toolbar, Box } from '@mui/material';
import { SxProps } from '@mui/system';
import { styled } from "@mui/material/styles";

import StyledAppBar from './Appbar';
import StyledDrawer from './Drawer';
import { useDrawer } from '../context/drawer/DrawerContext';

interface ContainerProps {
  main: React.ReactElement;
  secondary: React.ReactElement;
  toolbar: React.ReactElement;
  config: {
    drawerWidth: number;
    toolbarHeight: number;
  }
};



const mainStyle: (drawerOpen: boolean, drawerWidth: number) => SxProps = (drawerOpen, drawerWidth) => (drawerOpen ?
  { flexGrow: 1, overflow: "auto", height: "calc(100vh - 64px)", width: `calc(100vw - ${drawerWidth}px)` } :
  { flexGrow: 1, overflow: "auto", height: "calc(100vh - 64px)", marginLeft: '0px' });


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
      <Toolbar>
        {toolbarWindow}
      </Toolbar>
    </StyledAppBar>

    <StyledDrawer variant="permanent" open={drawerOpen} drawerWidth={components.config.drawerWidth}>
      <Toolbar />
      <Box sx={{ display: 'flex', overflowY: "scroll", height: "100vh" }}>
        {drawerOpen ? (<Box sx={{ width: components.config.drawerWidth, height: "100%" }}>{secondaryWindow}</Box>) : null}
      </Box>
    </StyledDrawer>

    <StyledMain>
      <Toolbar />
      <Box sx={mainStyle(drawerOpen, components.config.drawerWidth)}>{mainWindow}</Box>
    </StyledMain>
  </Box>);
}

export type { ContainerProps };
export { Container };
