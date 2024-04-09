import React from 'react';

import { Tabs, Tab, Box, TabProps, TabsProps, styled } from '@mui/material';

import FlipToFrontOutlinedIcon from '@mui/icons-material/FlipToFrontOutlined';
import DashboardIcon from '@mui/icons-material/Dashboard';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import FeedbackOutlinedIcon from '@mui/icons-material/FeedbackOutlined';
import TerminalIcon from '@mui/icons-material/Terminal';


import Burger from 'components-burger';
import { blueberry_whip, green_teal, sambucus } from 'components-colors';
import { useApp } from './useApp';


const StyledTab = styled(Tab)<TabProps>(({ theme }) => ({
  "&.MuiButtonBase-root": {
    minWidth: "unset",
    color: blueberry_whip,
  },
  "&.Mui-selected": {
    color: green_teal,
  }
}));

const StyledTabs = styled(Tabs)<TabsProps>(({ theme }) => ({
  "& .MuiTabs-indicator": {
    backgroundColor: green_teal,
    marginRight: "49px"
  }
}));




const Toolbar: React.FC<{}> = () => {
  const app = useApp();
  const drawer = Burger.useDrawer();
  const tabs = Burger.useTabs();
  const secondary = Burger.useSecondary();

  const tabActions = tabs.actions;
  const drawerOpen = drawer.session.drawer;
  React.useEffect(() => tabActions.handleTabAdd({ id: 'activities', label: "Activities" }), [tabActions]);


  const handleChange = (_event: React.SyntheticEvent, newValue: string) => {
    if (newValue === 'toolbar.activities') {
      tabs.actions.handleTabAdd({ id: 'activities', label: "Activities" });

    } else if (newValue === 'toolbar.tasks') {
      secondary.actions.handleSecondary("toolbar.tasks")

    } else if (newValue === 'toolbar.search') {
      secondary.actions.handleSecondary("toolbar.search")

    } else if (newValue === 'toolbar.import') {
      tabs.actions.handleTabAdd({ id: 'import', label: 'Import' })

    } else if (newValue === 'toolbar.expand') {
      drawer.actions.handleDrawerOpen(!drawerOpen)

    } else if (newValue === 'projects') {
      app.changeApp('frontoffice');
    }

  };


  return (
    <>
      <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', width: "100%", height: "100%", backgroundColor: sambucus }}>
        <StyledTabs orientation="vertical"
          onChange={handleChange}
          sx={{ borderRight: 1, borderColor: green_teal }}
          value={secondary.session.secondary}>

          <StyledTab value='projects' icon={<TerminalIcon />} />

          <StyledTab value='toolbar.activities' icon={<DashboardIcon />} />
          <StyledTab value='toolbar.help' icon={<HelpOutlineOutlinedIcon />} />
          <StyledTab value='toolbar.expand' icon={<FlipToFrontOutlinedIcon />} />
          <StyledTab value='feedback' icon={<FeedbackOutlinedIcon />} />

        </StyledTabs>
        <Box flexGrow={1} sx={{ borderRight: 1, borderColor: green_teal }} />

      </Box>
    </>
  );
}


export default Toolbar;