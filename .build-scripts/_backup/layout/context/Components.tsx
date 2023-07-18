import React from 'react';
import ReactResizeDetector from 'react-resize-detector';
import clsx from 'clsx';
import { CssBaseline, Drawer, AppBar, Toolbar as MuiToolbar, Box, Theme } from '@material-ui/core';
import { makeStyles } from '@material-ui/styles';

import Badges from '../badges';
import Tabs from '../tabs';
import Toolbar from '../toolbar';
import { HeaderSearch, HeaderRight, HeaderLeft } from '../header';


import { useLayout, Session } from './';


const useStyles = (props: { drawerWidth: number }) => makeStyles((theme: Theme) => ({
  root: {
    overflow: "hidden"
  },
  main: {
    width: '100%',
    height: '100vh',
    backgroundColor: theme.palette.background.default,
  },
  appBar: {
    zIndex: theme.zIndex.drawer + 1,
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
    backgroundColor: theme.palette.background.default,
  },
  appBarShift: {
    marginLeft: props.drawerWidth,
    width: `calc(100% - ${props.drawerWidth}px)`,
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  },
  drawerPaper: {
    height: '100vh',
    position: 'relative',
    whiteSpace: 'nowrap',
    width: props.drawerWidth,
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  },
  drawerPaperClose: {
    overflowX: 'hidden',
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
    width: theme.spacing(7),
  },
}))();


const Components: React.FC<{ children: Session.Configuration }> = ({ children }) => {
  const context = useLayout();
  const drawerWidth = context.session.drawer.width;
  const classes = useStyles({ drawerWidth });
  const header = children.header(context).page;
  const content = children.content(context).page;
  const toolbar = children.toolbar(context);
  const badges = children.badges(context);

  const drawer = {
    width: drawerWidth,
    open: context.session.drawer.open,
    onOpen: () => context.actions.handleDrawer(true),
    onClose: () => context.actions.handleDrawer()
  };
  const onResize = React.useCallback((width?: number, height?: number) => {
    if (!width || !height) {
      return;
    }

    if (context.session.dimensions.width !== width || context.session.dimensions.height !== height) {
      context.actions.handleDimensions({ width, height });
    }
  }, [context.actions, context.session.dimensions.height, context.session.dimensions.width]);

  return (
    <Box display="flex" className={classes.root}>
      <CssBaseline />
      <AppBar position="absolute" className={clsx(classes.appBar, drawer.open && classes.appBarShift)}>
        <MuiToolbar>
          <HeaderRight drawer={drawer}>
            <Tabs />
            <HeaderSearch onChange={(value, ref) => { context.actions.handleSearch(value); children.search(context, value, ref); }} />
            <Badges>{badges}</Badges>
          </HeaderRight>
        </MuiToolbar>
      </AppBar>

      <Drawer variant="permanent" open={drawer.open}
        classes={{
          paper: clsx(classes.drawerPaper, !drawer.open && classes.drawerPaperClose),
        }}>
        <HeaderLeft onDrawerClose={drawer.onClose}>{header}</HeaderLeft>
        <Toolbar drawer={drawer}>{toolbar}</Toolbar>
      </Drawer>

      <main className={classes.main}>
        <MuiToolbar />
        <ReactResizeDetector handleWidth handleHeight onResize={onResize} >
          {() => <div style={{ height: '100%' }}>{content}</div>}
        </ReactResizeDetector>
      </main>
    </Box>
  );
}

export { Components };
