import {
  Stack, SxProps, lighten, Typography, Theme, alpha, Box, darken, styled, Button, useTheme, AppBar, Toolbar
} from '@mui/material';


const StyledStartTaskButton = styled(Button)(({ theme }) => ({
  color: theme.palette.mainContent.main,
  fontWeight: 'bold',
  backgroundColor: theme.palette.uiElements.main,
  '&:hover': {
    backgroundColor: darken(theme.palette.uiElements.main, 0.3),
  }
}));


const StyledEditTaskButton = styled(Button)(({ theme }) => ({
  border: '1px solid',
  color: theme.palette.uiElements.main,
  fontWeight: 'bold',
  borderColor: theme.palette.uiElements.main,
  '&:hover': {
    borderColor: darken(theme.palette.uiElements.main, 0.3),
    color: darken(theme.palette.uiElements.main, 0.3)
  }
}));


const StyledStack: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const theme = useTheme();

  return (<Box sx={{
    height: '100%',
    position: 'fixed',
    width: '23%',
    boxShadow: 5,
    paddingTop: theme.spacing(2),
    paddingLeft: theme.spacing(2),
    paddingRight: theme.spacing(2),
    backgroundColor: theme.palette.background.paper
  }}>
    <Stack direction='column' spacing={1}>
      {children}
    </Stack>
  </Box>);
}


const StyledStackItem: React.FC<{
  index: number,
  children: React.ReactNode,
  onClick: () => void,
  active: boolean
}> = ({ active, onClick, children, index }) => {
  const theme = useTheme();

  function getStyles(index: number, active: boolean, theme: Theme): SxProps {
    const isOdd = index % 2 === 1;

    if (active) {
      return {
        p: 2,
        cursor: 'pointer',
        color: 'text.primary',
        backgroundColor: alpha(theme.palette.uiElements.main, 0.3),
        fontWeight: 'bolder',
        borderRadius: 1
      };
    }
    if (isOdd) {
      return {
        p: 2,
        cursor: 'pointer',
        backgroundColor: 'uiElements.light',
        color: 'text.primary'
      };
    } else {
      return {
        p: 2,
        cursor: 'pointer',
        backgroundColor: 'mainContent.main',
        color: 'text.primary'
      };
    }
  }

  return (
    <Box sx={getStyles(index, active, theme)} display='flex' alignItems='center'
      height={theme.typography.body2.fontSize} maxHeight={theme.typography.body2.fontSize} onClick={onClick}>
      {children}
    </Box>);
}

const StyledTaskListTab: React.FC<{ children: React.ReactNode, active: boolean, color: string, onClick: () => void }> = ({ children, active, color, onClick }) => {
  const backgroundColor = active ? color : 'unset';
  const border = active ? undefined : '1px solid' + color;
  const sx: SxProps = {
    borderRadius: '8px 8px 0px 0px',
    boxShadow: "unset",
    backgroundColor,
    border,
    color: active ? 'mainContent.main' : color,
    borderBottom: 'unset',
    '&:hover': {
      backgroundColor: active ? color : lighten(color, 0.2),
      color: 'mainContent.main'
    },
    ml: 1
  };

  return (<Button variant="contained" sx={sx} onClick={onClick}>
    <Typography sx={{ fontWeight: 'bolder' }}>
      {children}
    </Typography>
  </Button>);
}


const StyledAppBar: React.FC<{ children: React.ReactNode, color: string | undefined }> = ({ children, color }) => {
  return (<AppBar color='inherit' position='sticky' sx={{ boxShadow: 'unset', px: 1, borderBottom: '1px solid' + color }}>
    <Toolbar sx={{ alignItems: 'end', "&.MuiToolbar-root": { px: 'unset', minHeight: 50 } }}>
      {children}
    </Toolbar>
  </AppBar >);
}


export { StyledStackItem, StyledTaskListTab, StyledStartTaskButton, StyledEditTaskButton, StyledStack, StyledAppBar }