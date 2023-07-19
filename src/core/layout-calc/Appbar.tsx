import { styled, AppBar, AppBarProps } from '@mui/material';



interface StyledAppBarProps extends AppBarProps {
}

const StyledAppBar = styled(AppBar, {

})<StyledAppBarProps>(({ theme }) => ({
 // width: `calc(100% - ${drawerWidth.collapsed + 1}px)`,
  backgroundColor: theme.palette.background.default,
  zIndex: theme.zIndex.drawer + 1,
}));


export default StyledAppBar;
