import React from 'react';
import { Tabs, Tab, Box, TabProps, TabsProps, styled } from '@mui/material';

import FlipToFrontOutlinedIcon from '@mui/icons-material/FlipToFrontOutlined';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';

import * as Burger from '@/burger';
import { LocaleSelect } from './explorer';




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
  const drawerCtx = Burger.useDrawer();
  const secondaryCtx = Burger.useSecondary();

  const drawerOpen = drawerCtx.session.drawer;

  const handleChange = (_event: React.SyntheticEvent, newValue: string) => {
    if (newValue === 'toolbar.expand') {
      drawerCtx.actions.handleDrawerOpen(!drawerOpen)
    }
  };

  return (
    <>
      <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', width: "100%", height: "100%", backgroundColor: "explorer.main" }}>

        <StyledTabs orientation="vertical"
          onChange={handleChange}
          sx={{ borderRight: 1, borderColor: 'explorerItem.dark' }}
          value={secondaryCtx.session.secondary}>

          <StyledTab value='toolbar.expand' icon={<FlipToFrontOutlinedIcon />} />
          <StyledTab value='toolbar.help' icon={<HelpOutlineOutlinedIcon onClick={() => window.open("https://google.com", "_blank")} />} />
          <LocaleSelect />

        </StyledTabs>
        <Box flexGrow={1} sx={{ borderRight: 1, borderColor: 'explorerItem.dark' }} />
      </Box>
    </>
  );
}


