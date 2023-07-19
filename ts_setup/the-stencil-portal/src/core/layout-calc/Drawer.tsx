import { CSSObject, styled, Drawer, Theme, DrawerProps } from '@mui/material';

interface StyledDrawerProps extends DrawerProps {
  drawerWidth: number | string;
}


const openedMixin = (theme: Theme, drawerWidth: string): CSSObject => ({
  width: drawerWidth,
  transition: theme.transitions.create('width', {
    easing: theme.transitions.easing.sharp,
    duration: theme.transitions.duration.enteringScreen,
  }),
});

const closedMixin = (theme: Theme): CSSObject => ({
  transition: theme.transitions.create('width', {
    easing: theme.transitions.easing.sharp,
    duration: theme.transitions.duration.leavingScreen,
  }),
  width: "0px",
  visibility: 'hidden'
});


const StyledDrawer = styled(Drawer, {
  shouldForwardProp: (prop) => prop !== 'open' && prop !== 'drawerWidth',
})<StyledDrawerProps>(
  ({ theme, open, drawerWidth }) => ({
    //width: `calc(${typeof drawerWidth === 'number' ? drawerWidth + 'px' : drawerWidth})`,
    flexShrink: 0,
    whiteSpace: 'nowrap',
    boxSizing: 'border-box',
    ...(open && {
      ...openedMixin(theme, `calc(${typeof drawerWidth === 'number' ? drawerWidth + 'px' : drawerWidth})`),
      '& .MuiDrawer-paper': openedMixin(theme, `calc(${typeof drawerWidth === 'number' ? drawerWidth + 'px' : drawerWidth} + 1px)`),
    }),
    ...(!open && {
      ...closedMixin(theme),
      '& .MuiDrawer-paper': closedMixin(theme),
    }),
  }),
);

export default StyledDrawer;
