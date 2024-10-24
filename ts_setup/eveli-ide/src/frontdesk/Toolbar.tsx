import React from 'react';
import { Tabs, Tab, Box, TabProps, TabsProps, styled } from '@mui/material';

import FlipToFrontOutlinedIcon from '@mui/icons-material/FlipToFrontOutlined';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import DashboardIcon from '@mui/icons-material/Dashboard';
import ListIcon from '@mui/icons-material/ListAlt';
import BuildIcon from '@mui/icons-material/Build';
import ViewListIcon from '@mui/icons-material/ViewList';
import SettingsIcon from '@mui/icons-material/Settings';
import BusinessCenterIcon from '@mui/icons-material/BusinessCenter';
import NetworkCheckIcon from '@mui/icons-material/NetworkCheck';
import AllInboxIcon from '@mui/icons-material/AllInbox';
import SubjectIcon from '@mui/icons-material/Subject';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import { FormattedMessage } from 'react-intl';

import * as Burger from '@/burger';





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

  const tabsCtx = Burger.useTabs();
  const drawerCtx = Burger.useDrawer();
  const secondaryCtx = Burger.useSecondary();

  const drawerOpen = drawerCtx.session.drawer;
  const tabsActions = tabsCtx.actions;
  const secondaryActions = secondaryCtx.actions;


  //TODO active tab

  const active = tabsCtx.session.tabs.length ? tabsCtx.session.tabs[tabsCtx.session.history.open] : undefined;

  //TODO

  const handleChange = (_event: React.SyntheticEvent, newValue: string) => {

    if (newValue === 'toolbar.activities') {
      tabsActions.handleTabAdd({ id: 'newItem', label: "Activities" });

    } else if (newValue === 'toolbar.articles') {
      secondaryCtx.actions.handleSecondary("toolbar.articles")

    } else if (newValue === 'toolbar.search') {
      secondaryCtx.actions.handleSecondary("toolbar.search")

    } else if (newValue === 'toolbar.import') {
      tabsActions.handleTabAdd({ id: 'import', label: 'Import' })

    } else if (newValue === 'toolbar.expand') {
      drawerCtx.actions.handleDrawerOpen(!drawerOpen)
    }
  };

  //TODO
  // open dashboard
  React.useLayoutEffect(() => {
    console.log("init toolbar");
    secondaryActions.handleSecondary("toolbar.articles")
    //tabsActions.handleTabAdd({ id: 'newItem', label: "Activities" });
  }, [tabsActions, secondaryActions]);


  return (
    <>
      <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', width: "100%", height: "100%", backgroundColor: "explorer.main" }}>
        <StyledTabs orientation="vertical"
          onChange={handleChange}
          sx={{ borderRight: 1, borderColor: 'explorerItem.dark' }}
          value={secondaryCtx.session.secondary}>

          <StyledTab value='toolbar.tasks' icon={<ViewListIcon />} />
          <StyledTab value='toolbar.dashboard' icon={<DashboardIcon />} />
          <StyledTab value='toolbar.monitoring' icon={<NetworkCheckIcon />} />
          <StyledTab value='toolbar.forms' icon={<ListIcon />} />
          <StyledTab value='toolbar.flow' icon={<BuildIcon />} />
          <StyledTab value='toolbar.content' icon={<SubjectIcon />} />
          <StyledTab value='toolbar.calendar' icon={<CalendarMonthIcon />} />
          <StyledTab value='toolbar.workflows' icon={<SettingsIcon />} />
          <StyledTab value='toolbar.workflowReleases' icon={<AllInboxIcon />} />
          <StyledTab value='toolbar.releases' icon={<BusinessCenterIcon />} />
          <StyledTab value='toolbar.help' icon={<HelpOutlineOutlinedIcon onClick={() => window.open("https://google.com", "_blank")} />} />
          <StyledTab value='toolbar.expand' icon={<FlipToFrontOutlinedIcon />} />

        </StyledTabs>
        <Box flexGrow={1} sx={{ borderRight: 1, borderColor: 'explorerItem.dark' }} />
        {/* <LocaleFilter /> */}

      </Box>
    </>
  );
}


