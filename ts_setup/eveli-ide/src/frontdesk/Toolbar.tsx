import React from 'react';
import { Tabs, Tab, Box, TabProps, TabsProps, styled, Tooltip } from '@mui/material';

import FlipToFrontOutlinedIcon from '@mui/icons-material/FlipToFrontOutlined';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import { useIntl } from 'react-intl';

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

  const intl = useIntl();
  const tabsCtx = Burger.useTabs();
  const drawerCtx = Burger.useDrawer();
  const secondaryCtx = Burger.useSecondary();

  const drawerOpen = drawerCtx.session.drawer;
  const tabsActions = tabsCtx.actions;
  const secondaryActions = secondaryCtx.actions;


  const handleChange = (_event: React.SyntheticEvent, newValue: string) => {
    if (newValue === 'toolbar.expand') {
      drawerCtx.actions.handleDrawerOpen(!drawerOpen)
    }
  };

  //TODO
  // open dashboard
  React.useLayoutEffect(() => {
    console.log("init toolbar");
    // secondaryActions.handleSecondary("toolbar.tasks")
    //tabsActions.handleTabAdd({ id: 'newItem', label: "Tasks" });
  }, [tabsActions, secondaryActions]);


  return (
    <>
      <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', width: "100%", height: "100%", backgroundColor: "explorer.main" }}>

        <StyledTabs orientation="vertical"
          onChange={handleChange}
          sx={{ borderRight: 1, borderColor: 'explorerItem.dark' }}
          value={secondaryCtx.session.secondary}>

          <Tooltip title={intl.formatMessage({ id: 'explorer.toolbar.expand.msg' })}>
            <StyledTab value='toolbar.expand' icon={<FlipToFrontOutlinedIcon />} />
          </Tooltip>

          <Tooltip title={intl.formatMessage({ id: 'explorer.toolbar.help.msg' })}>
            <StyledTab value='toolbar.help' icon={<HelpOutlineOutlinedIcon onClick={() => window.open("https://google.com", "_blank")} />} />
          </Tooltip>

        </StyledTabs>
        <Box flexGrow={1} sx={{ borderRight: 1, borderColor: 'explorerItem.dark' }} />
        {/* <LocaleFilter /> */}

      </Box>
    </>
  );
}


