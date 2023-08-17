import React from 'react';
import { Tabs, Tab, Box, TabProps, TabsProps } from '@mui/material';
import { styled } from "@mui/material/styles";
import Burger from '@digiexpress/react-burger';
import FlipToFrontOutlinedIcon from '@mui/icons-material/FlipToFrontOutlined';
import ArticleOutlinedIcon from '@mui/icons-material/ArticleOutlined';
import DashboardIcon from '@mui/icons-material/Dashboard';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import SearchOutlinedIcon from '@mui/icons-material/SearchOutlined';
import FeedbackOutlinedIcon from '@mui/icons-material/FeedbackOutlined';



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




const Toolbar: React.FC<{children: React.ReactNode}> = ({children}) => {
  const tabsCtx = Burger.useTabs();
  const drawerCtx = Burger.useDrawer();
  const secondaryCtx = Burger.useSecondary();
  
  const drawerOpen = drawerCtx.session.drawer;
  const tabsActions = tabsCtx.actions;
  const secondaryActions = secondaryCtx.actions;

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


  // open dashboard
  React.useLayoutEffect(() => {
    console.log("init toolbar");
    secondaryActions.handleSecondary("toolbar.articles")
    tabsActions.handleTabAdd({ id: 'newItem', label: "Activities" });
  }, [tabsActions, secondaryActions]);


  return (
    <>
      <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', width: "100%", height: "100%", backgroundColor: "explorer.main" }}>
        <StyledTabs orientation="vertical"
          onChange={handleChange}
          sx={{ borderRight: 1, borderColor: 'explorerItem.dark' }}
          value={secondaryCtx.session.secondary}>

          <StyledTab value='toolbar.activities' icon={<DashboardIcon />} />
          <StyledTab value='toolbar.search' icon={<SearchOutlinedIcon />} />
          <StyledTab value='toolbar.articles' icon={<ArticleOutlinedIcon />} />
          <StyledTab value='toolbar.help' icon={<HelpOutlineOutlinedIcon />} />
          <StyledTab value='toolbar.expand' icon={<FlipToFrontOutlinedIcon />} />
          <StyledTab value='feedback' icon={<FeedbackOutlinedIcon />} />

        </StyledTabs>
        <Box flexGrow={1} sx={{ borderRight: 1, borderColor: 'explorerItem.dark' }} />
        {children}
      </Box>
    </>
  );
}


export default Toolbar;