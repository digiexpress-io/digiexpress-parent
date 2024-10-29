import React from 'react';
import { Tabs, Tab, Box, TabProps, TabsProps, styled, Button, useTheme } from '@mui/material';

import FlipToFrontOutlinedIcon from '@mui/icons-material/FlipToFrontOutlined';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import FeedbackOutlinedIcon from '@mui/icons-material/FeedbackOutlined';

import * as Burger from '@/burger';
import { LocaleSelect } from './explorer';
import { FeedbackContext } from './context/FeedbackContext';




const StyledTab = styled(Tab)<TabProps>(({ theme }) => ({
  "&.MuiButtonBase-root": {
    minWidth: "unset",
    color: theme.palette.explorerItem.main,
  },
  "&.Mui-selected": {
    color: theme.palette.explorerItem.dark,
  }
}));

const StyledTabs = styled(Tabs)<TabsProps>(({ theme }) => ({
  "& .MuiTabs-indicator": {
    backgroundColor: theme.palette.explorerItem.dark,
    marginRight: "49px"
  }
}));




export const Toolbar: React.FC<{}> = () => {
  const theme = useTheme();
  const drawerCtx = Burger.useDrawer();
  const drawerOpen = drawerCtx.session.drawer;
  const context = React.useContext(FeedbackContext);

  const handleChange = (_event: React.SyntheticEvent, newValue: string) => {
    if (newValue === 'toolbar.expand') {
      drawerCtx.actions.handleDrawerOpen(!drawerOpen)
    }
  };

  const openFeedback = () => {
    context.open();
  }

  return (
    <>
      <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', width: "100%", height: "100%", backgroundColor: "explorer.main" }}>

        <StyledTabs orientation="vertical"
          onChange={handleChange}
          sx={{ borderRight: 1, borderColor: 'explorerItem.dark' }}
          value={false}>
          <StyledTab value='toolbar.expand' icon={<FlipToFrontOutlinedIcon />} />
          <StyledTab value='toolbar.help' icon={<HelpOutlineOutlinedIcon onClick={() => window.open("https://google.com", "_blank")} />} />

          {/*userInfo.isAuthenticated() && (ENV_TYPE !== 'prod' || userInfo.hasRole(...FEEDBACK_ROLES)) && */}
          <Button variant='text' onClick={openFeedback} sx={{ my: 1 }}>
            <FeedbackOutlinedIcon sx={{ color: theme.palette.explorerItem.main }} />
          </Button>
          <LocaleSelect />
        </StyledTabs>

        <Box flexGrow={1} sx={{ borderRight: 1, borderColor: 'explorerItem.dark' }} />
      </Box>
    </>
  );
}


