import React from 'react';

import { CssBaseline, Toolbar as MToolbar, Box } from '@mui/material';
import { SxProps } from '@mui/system';
import { styled } from "@mui/material/styles";

import StyledAppBar from './Appbar';
import StyledDrawer from './Drawer';
import { useDrawer } from '../context/drawer/DrawerContext';
import { Tabs } from './Tabs';

interface ContainerProps {
  main: React.ReactElement;
  secondary: React.ReactElement;
  toolbar: React.ReactElement;
  tabs: boolean;
};

const drawerWidth = { expanded: 500, collapsed: 56 };
const toolbarStyle: SxProps = {
  backgroundColor: "primary.main",
  color: "primary.contrastText",
  overflow: "hidden",
  position: "fixed",
  height: "100%"
};
const secondaryStyle: SxProps = {
  width: drawerWidth.expanded,
  marginLeft: `${drawerWidth.collapsed + 1}px`,
  height: "100%"
};
const mainStyle: (drawerOpen: boolean) => SxProps = (drawerOpen) => (drawerOpen ?
  { flexGrow: 1, overflow: "auto", height: "calc(100vh - 64px)", width: "calc(100vw - 500px)" } :
  { flexGrow: 1, overflow: "auto", marginLeft: '57px', height: "calc(100vh - 64px)" });

const drawerStyle: SxProps = { display: 'flex', overflowY: "scroll", height: "100vh" };


const StyledMain = styled("main")(() => ({
  width: "100%",
  height: "100%"
}));

const Toolbar: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const layout = useDrawer();
  const drawerOpen = layout.session.drawer;
  return (
    <StyledAppBar position="fixed" open={drawerOpen} drawerWidth={drawerWidth}>
      <MToolbar>{children}</MToolbar>
    </StyledAppBar>
  );
}


const Container: React.FC<ContainerProps> = (components) => {
  const layout = useDrawer();
  const drawerOpen = layout.session.drawer;
  const { main, secondary, toolbar, tabs } = components;

  const mainWindow = React.useMemo(() => main, [main]);
  const secondaryWindow = React.useMemo(() => secondary, [secondary]);
  const toolbarWindow = React.useMemo(() => toolbar, [toolbar]);
  const tabsWindow = React.useMemo(() => tabs, [tabs]);

  return (<Box sx={{ display: 'flex', height: "100vh" }}>
    <CssBaseline />

    {tabsWindow && (<Toolbar><Tabs /></Toolbar>) }

    <StyledDrawer variant="permanent" open={drawerOpen} drawerWidth={drawerWidth}>
      <Box sx={drawerStyle}>
        <Box sx={toolbarStyle}>{toolbarWindow}</Box>
        {drawerOpen ? (<Box sx={secondaryStyle}>{secondaryWindow}</Box>) : null}
      </Box>
    </StyledDrawer>

    <StyledMain>
      <MToolbar />
      <Box sx={mainStyle(drawerOpen)}>{mainWindow}</Box>
    </StyledMain>
  </Box>);
}

export type { ContainerProps };
export { Container, Toolbar };
