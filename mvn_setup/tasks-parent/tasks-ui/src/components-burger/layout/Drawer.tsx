import { CSSObject, styled, Drawer, Theme, DrawerProps } from '@mui/material';

interface StyledDrawerProps extends DrawerProps {
  drawerWidth: { collapsed: number, expanded: number};
}


const openedMixin = (theme: Theme, drawerWidth: { collapsed: number, expanded: number}): CSSObject => ({
  width: drawerWidth.expanded -1,
  transition: theme.transitions.create('width', {
    easing: theme.transitions.easing.sharp,
    duration: theme.transitions.duration.enteringScreen,
  }),
  overflowX: 'hidden',
});

const closedMixin = (theme: Theme, drawerWidth: { collapsed: number, expanded: number}): CSSObject => ({
  transition: theme.transitions.create('width', {
    easing: theme.transitions.easing.sharp,
    duration: theme.transitions.duration.leavingScreen,
  }),
  overflowX: 'hidden',
  width: `calc(${drawerWidth.collapsed} + 1px)`
});


const StyledDrawer = styled(Drawer, {
  shouldForwardProp: (prop) => prop !== 'open' && prop !== 'drawerWidth',
})<StyledDrawerProps>(
  ({ theme, open, drawerWidth }) => ({
    width: drawerWidth.expanded - 1,
    flexShrink: 0,
    whiteSpace: 'nowrap',
    boxSizing: 'border-box',
    ...(open && {
      ...openedMixin(theme, drawerWidth),
      '& .MuiDrawer-paper': openedMixin(theme, drawerWidth),
    }),
    ...(!open && {
      ...closedMixin(theme, drawerWidth),
      '& .MuiDrawer-paper': closedMixin(theme, drawerWidth),
    }),
  }),
);

export default StyledDrawer;
