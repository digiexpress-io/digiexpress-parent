import React from 'react';

import { Tabs, Tab, Box, TabProps, TabsProps } from '@mui/material';
import { styled } from "@mui/material/styles";


import { FormattedMessage } from 'react-intl';
import FlipToFrontOutlinedIcon from '@mui/icons-material/FlipToFrontOutlined';
import ArticleOutlinedIcon from '@mui/icons-material/ArticleOutlined';
import DashboardIcon from '@mui/icons-material/Dashboard';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import SearchOutlinedIcon from '@mui/icons-material/SearchOutlined';
import SaveIcon from '@mui/icons-material/Save';
import FeedbackOutlinedIcon from '@mui/icons-material/FeedbackOutlined';
import TerminalIcon from '@mui/icons-material/Terminal';
import { useSnackbar } from 'notistack';

import Burger from 'components-burger';
import { Composer } from './context';
import { blueberry_whip, green_teal, saffron, sambucus } from 'components-colors';
import { useApp } from 'app-frontoffice/Views/useApp';


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
  const composer = Composer.useComposer();
  const drawer = Burger.useDrawer();
  const tabs = Burger.useTabs();
  const app = useApp();
  const secondary = Burger.useSecondary();
  const { enqueueSnackbar } = useSnackbar();
  
  const tabActions = tabs.actions;  
  const drawerOpen = drawer.session.drawer;
  React.useEffect(() => tabActions.handleTabAdd({ id: 'activities', label: "Activities" }), [tabActions]);
  

  //const articlePagesView = active?.data?.nav?.type === "ARTICLE_PAGES";
  const unsavedPages = Object.values(composer.session.pages).filter(p => !p.saved);
  const saveSx = unsavedPages.length ? { color: saffron } : undefined;


  const handleChange = (_event: React.SyntheticEvent, newValue: string) => {
    if (newValue === 'toolbar.save' && unsavedPages) {
      if (unsavedPages.length === 0) {
        return;
      }
      const active = tabs.session.tabs.length ? tabs.session.tabs[tabs.session.history.open] : undefined;
      
      const article = active ? composer.session.getEntity(active.id) : undefined;
      if(!article) {
        return;
      }
      const toBeSaved = unsavedPages.filter(p => !p.saved).filter(p => p.origin.id === article.id);
      if(toBeSaved.length !== 1) {
        return;
      }
      
      const unsavedArticlePages: Composer.PageUpdate = toBeSaved[0];
      composer.service.update(article.id, unsavedArticlePages.value).then(success => {
        composer.actions.handlePageUpdateRemove([article.id]);
        enqueueSnackbar(<FormattedMessage id="activities.assets.saveSuccess" values={{ name: article.ast?.name }} />);
        composer.actions.handleLoadSite(success);
      }).catch((error) => {
        
      });

    } else if (newValue === 'toolbar.activities') {
      tabs.actions.handleTabAdd({ id: 'activities', label: "Activities" });

    } else if (newValue === 'toolbar.assets') {
      secondary.actions.handleSecondary("toolbar.assets")

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
          <StyledTab value='toolbar.save'
            icon={<SaveIcon sx={saveSx} />}
            disabled={unsavedPages.length === 0}
            label={unsavedPages.length ? (<Box sx={saveSx}>{unsavedPages.length}</Box>) : undefined} />
          <StyledTab value='toolbar.search' icon={<SearchOutlinedIcon />} />
          <StyledTab value='toolbar.assets' icon={<ArticleOutlinedIcon />} />
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