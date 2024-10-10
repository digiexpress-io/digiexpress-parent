import React from 'react';
import { Box } from '@mui/material';
import Burger from '@the-wrench-io/react-burger';
import {
  ActivitiesView, ArticlePageComposer, ArticleWorkflowsComposer, ArticleLinksComposer, WorkflowsView,
  ReleasesView, LocalesView, ReleaseGraph,
} from './';

import {
  TemplatesView
} from './template';


import { Composer } from './context';


const root = { height: `100%`, padding: 1, backgroundColor: "mainContent.main" };

const Main: React.FC<{}> = () => {
  const layout = Burger.useTabs();
  const site = Composer.useSite();
  const tabs = layout.session.tabs;
  const active = tabs.length ? tabs[layout.session.history.open] : undefined;

  //composers which are NOT linked directly with an article

  return React.useMemo(() => {
    if (site.contentType === "NO_CONNECTION") {
      return (<Box>{site.contentType}</Box>);
    }
    if (!active) {
      return null;
    }
    console.log("init main");

    if (active.id === 'releases') {
      return (<Box sx={root}><ReleasesView /></Box>);
    } else if (active.id === 'newItem') {
      return (<Box sx={root}><ActivitiesView /></Box>);
    } else if (active.id === 'locales') {
      return (<Box sx={root}><LocalesView /></Box>);
    } else if (active.id === 'workflows') {
      return (<Box sx={root}><WorkflowsView /></Box>);
    } else if (active.id === 'graph') {
      return (<Box sx={root}><ReleaseGraph /></Box>);
    } else if (active.id === 'templates') {
      return (<Box sx={root}><TemplatesView /></Box>);
    }

    //article-based composers
    const article = site.articles[active.id];
    const tab: Composer.Tab = active;
    if (!tab.data || !tab.data.nav) {
      return null;
    }

    let composer: React.ReactChild;
    if (tab.data.nav.type === "ARTICLE_PAGES") {
      const locale1 = tab.data.nav.value as string;
      const locale2 = tab.data.nav.value2 as string;
      composer = (<ArticlePageComposer key={article.id + "-" + locale1 + "-" + locale2} articleId={article.id} locale1={locale1} locale2={locale2} />);
    } else if (tab.data.nav.type === "ARTICLE_LINKS") {
      composer = (<ArticleLinksComposer key={article.id + "-links"} articleId={article.id} />)
    } else if (tab.data.nav.type === "ARTICLE_WORKFLOWS") {
      composer = (<ArticleWorkflowsComposer key={article.id + "-workflows"} articleId={article.id} />)
    } else {
      composer = (<></>);
    }

    return (<Box sx={root} key={article.id}>{composer}</Box>)


  }, [active, site]);
}
export { Main }


