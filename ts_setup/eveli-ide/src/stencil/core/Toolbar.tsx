import React from 'react';
import { useSnackbar } from 'notistack';

import { Tabs, Tab, Box, TabProps, TabsProps } from '@mui/material';
import { styled } from "@mui/material/styles";
import { FormattedMessage } from 'react-intl';
import Burger from '@the-wrench-io/react-burger';
import FlipToFrontOutlinedIcon from '@mui/icons-material/FlipToFrontOutlined';
import ArticleOutlinedIcon from '@mui/icons-material/ArticleOutlined';
import DashboardIcon from '@mui/icons-material/Dashboard';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import SearchOutlinedIcon from '@mui/icons-material/SearchOutlined';
import SaveIcon from '@mui/icons-material/Save';
import FeedbackOutlinedIcon from '@mui/icons-material/FeedbackOutlined';

import { Composer, StencilClient } from './context';
import { LocaleFilter } from './explorer/filter';


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




const Toolbar: React.FC<{}> = () => {
  const { enqueueSnackbar } = useSnackbar();
  const composer = Composer.useComposer();
  const tabsCtx = Burger.useTabs();
  const drawerCtx = Burger.useDrawer();
  const secondaryCtx = Burger.useSecondary();
  
  const drawerOpen = drawerCtx.session.drawer;
  const tabsActions = tabsCtx.actions;
  const secondaryActions = secondaryCtx.actions;
  
  

  const active = tabsCtx.session.tabs.length ? tabsCtx.session.tabs[tabsCtx.session.history.open] : undefined;
  const article = active ? composer.site.articles[active.id] : undefined;
  const articlePagesView = active?.data?.nav?.type === "ARTICLE_PAGES";
  const unsavedPages = Object.values(composer.session.pages).filter(p => !p.saved);
  const unsavedArticlePages: Composer.PageUpdate[] = (article ? unsavedPages.filter(p => !p.saved).filter(p => p.origin.body.article === article.id) : []);

  const message = <FormattedMessage id="snack.page.savedMessage" />

  const handleChange = (_event: React.SyntheticEvent, newValue: string) => {

    if (newValue === 'toolbar.save' && articlePagesView && article) {
      if (unsavedArticlePages.length === 0) {
        return;
      }
      const update: StencilClient.PageMutator[] = unsavedArticlePages.map(p => ({ pageId: p.origin.id, locale: p.origin.body.locale, content: p.value, devMode: p.origin.body.devMode }));
      composer.service.update().pages(update).then(success => {
        enqueueSnackbar(message, {variant: 'success'});
        composer.actions.handlePageUpdateRemove(success.map(p => p.id));
      }).then(() => {
        composer.actions.handleLoadSite();
      });


    } else if (newValue === 'toolbar.activities') {
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

  const saveSx = unsavedPages.length ? { color: "explorerItem.contrastText" } : undefined;

  return (
    <>
      <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', width: "100%", height: "100%", backgroundColor: "explorer.main" }}>
        <StyledTabs orientation="vertical"
          onChange={handleChange}
          sx={{ borderRight: 1, borderColor: 'explorerItem.dark' }}
          value={secondaryCtx.session.secondary}>

          <StyledTab value='toolbar.activities' icon={<DashboardIcon />} />
          <StyledTab value='toolbar.save'
            icon={<SaveIcon sx={saveSx} />}
            disabled={unsavedArticlePages.length === 0}
            label={unsavedPages.length ? (<Box sx={saveSx}>{unsavedPages.length}</Box>) : undefined} />
          <StyledTab value='toolbar.search' icon={<SearchOutlinedIcon />} />
          <StyledTab value='toolbar.articles' icon={<ArticleOutlinedIcon />} />
          <StyledTab value='toolbar.help' icon={<HelpOutlineOutlinedIcon onClick={() => window.open("https://github.com/the-stencil-io/the-stencil-composer/wiki", "_blank")} />} />
          <StyledTab value='toolbar.expand' icon={<FlipToFrontOutlinedIcon />} />
          <StyledTab value='feedback' icon={<FeedbackOutlinedIcon />} />

        </StyledTabs>
        <Box flexGrow={1} sx={{ borderRight: 1, borderColor: 'explorerItem.dark' }} />
        <LocaleFilter />

      </Box>
    </>
  );
}


export default Toolbar;