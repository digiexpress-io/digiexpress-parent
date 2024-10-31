import React from 'react';
import { Box, styled } from '@mui/material';

import FlipToFrontOutlinedIcon from '@mui/icons-material/FlipToFrontOutlined';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import FeedbackOutlinedIcon from '@mui/icons-material/FeedbackOutlined';

import * as Burger from '@/burger';
import { LocaleSelect } from './explorer';
import { FeedbackContext } from './context/FeedbackContext';



const StyledToolbarButton = styled(Box)(({ theme }) => ({
  display: 'flex',
  justifyContent: 'center',
  margin: theme.spacing(1.5),
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
  const drawerCtx = Burger.useDrawer();
  const drawerOpen = drawerCtx.session.drawer;
  const context = React.useContext(FeedbackContext);


  const openFeedback = () => {
    context.open();
  }
  const toggleDrawer = () => {
    drawerCtx.actions.handleDrawerOpen(!drawerOpen);
  };

  return (
    <>
      <StyledToolbar>
        <StyledToolbarButton onClick={toggleDrawer}><FlipToFrontOutlinedIcon /></StyledToolbarButton>
        <StyledToolbarButton onClick={() => window.open("https://google.com", "_blank")}><HelpOutlineOutlinedIcon /></StyledToolbarButton>
        {/*TODO Feedback still needed??  */}
        { /*<StyledToolbarButton onClick={openFeedback}><FeedbackOutlinedIcon /></StyledToolbarButton> */}
        <LocaleSelect />
        {/*userInfo.isAuthenticated() && (ENV_TYPE !== 'prod' || userInfo.hasRole(...FEEDBACK_ROLES)) && */}
      </StyledToolbar>
    </>
  );
}


