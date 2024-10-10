import { styled, AppBar, AppBarProps } from '@mui/material';



interface StyledAppBarProps extends AppBarProps {
  open?: boolean;
  drawerWidth: { collapsed: number, expanded: number};
}

const StyledAppBar = styled(AppBar, {
  shouldForwardProp: (prop) => prop !== 'open' && prop !== 'drawerWidth',
})<StyledAppBarProps>(({ theme, open, drawerWidth }) => ({
  width: `calc(100% - ${drawerWidth.collapsed + 1}px)`,
  backgroundColor: theme.palette.background.default,
  zIndex: theme.zIndex.drawer + 1,
  transition: theme.transitions.create(['width', 'margin'], {
    easing: theme.transitions.easing.sharp,
    duration: theme.transitions.duration.leavingScreen,
  }),
  ...(open && {
    marginLeft: drawerWidth.expanded,
    width: `calc(100% - ${drawerWidth.expanded-1}px)`,
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  }),
}));


export default StyledAppBar;
