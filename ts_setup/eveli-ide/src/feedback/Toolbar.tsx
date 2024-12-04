import React from 'react';
import { Box, styled } from '@mui/material';

import FlipToFrontOutlinedIcon from '@mui/icons-material/FlipToFrontOutlined';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import HomeOutlinedIcon from '@mui/icons-material/HomeOutlined';

import * as Burger from '@/burger';
import { useNavigate } from 'react-router-dom';




const StyledToolbarButton = styled(Box)(({ theme }) => ({
  display: 'flex',
  justifyContent: 'center',
  margin: theme.spacing(2),
  ':hover': {
    cursor: 'pointer'
  },
  '& .MuiSvgIcon-root': {
    color: theme.palette.explorerItem.main
  }
}));



const StyledToolbar = styled(Box)(({ theme }) => ({
  flexGrow: 1,
  display: 'flex',
  width: "100%",
  height: "100%",
  flexDirection: 'column',
  borderRight: `1px solid ${theme.palette.explorerItem.dark}`,
  backgroundColor: theme.palette.explorer.main
}))

export const Toolbar: React.FC<{}> = () => {
  const navigate = useNavigate();
  const drawerCtx = Burger.useDrawer();
  const drawerOpen = drawerCtx.session.drawer;

  function handleBacktoTasks() {
    navigate('/ui/tasks');
  }

  const toggleDrawer = () => {
    drawerCtx.actions.handleDrawerOpen(!drawerOpen);
  };

  return (
    <>
      <StyledToolbar>
        <StyledToolbarButton onClick={toggleDrawer}><FlipToFrontOutlinedIcon /></StyledToolbarButton>
        <StyledToolbarButton onClick={() => window.open("https://google.com", "_blank")}><HelpOutlineOutlinedIcon /></StyledToolbarButton>
        <StyledToolbarButton onClick={handleBacktoTasks}><HomeOutlinedIcon /></StyledToolbarButton>

      </StyledToolbar>
    </>
  );
}


