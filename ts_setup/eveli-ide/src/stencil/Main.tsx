import React from 'react';
import { Box } from '@mui/material';
import * as Burger from '@/burger';
import {
  ActivitiesView, ArticlePageComposer, ArticleWorkflowsComposer, ArticleLinksComposer, WorkflowsView,
  ReleasesView, LocalesView, ArticlesView
} from './';
import { TemplatesView } from './template';
import { Composer } from './context';
import { LinksView } from './link';
import { ExplorerItem } from './nav';

const root = { height: `100%`, padding: 1, backgroundColor: "primary.contrastText" };

const Main: React.FC<{}> = () => {
  const { session: tabs } = Burger.useTabs();
  const activeTab = tabs.activeTab;
  const site = Composer.useSite();

  return React.useMemo(() => {
    if (site.contentType === "NO_CONNECTION") {
      return (<Box>{site.contentType}</Box>);
    }
    if (!activeTab) {
      return (<Box sx={root}></Box>)
    }
    const explorer: ExplorerItem | undefined = activeTab.data;
    if(!explorer) {
      return (<Box sx={root}></Box>)
    }

    switch(explorer.type) {
      case 'RELEASES': return (<Box sx={root}><ReleasesView /></Box>);
      case 'ACTIVITIES': return (<Box sx={root}><ActivitiesView /></Box>);
      case 'LOCALES': return (<Box sx={root}><LocalesView /></Box>);
      case 'SERVICES': return (<Box sx={root}><WorkflowsView /></Box>);
      case 'TEMPLATES': return (<Box sx={root}><TemplatesView /></Box>);
      case 'ARTICLES': return (<Box sx={root}><ArticlesView /></Box>);
      case 'LINKS': return (<Box sx={root}><LinksView /></Box>);
      case 'ARTICLE_PAGES': {
        const { article, locale1, locale2} = explorer;
        return (<Box sx={root} key={article}><ArticlePageComposer key={article + "-" + locale1 + "-" + locale2} articleId={article} locale1={locale1} locale2={locale2} /></Box>);
      }
      case 'ARTICLE_LINKS': return (<Box sx={root}><ArticleLinksComposer key={explorer.article + "-links"} articleId={explorer.article} /></Box>)
      case 'ARTICLE_WORKFLOWS': return (<Box sx={root}><ArticleWorkflowsComposer key={explorer.article + "-workflows"} articleId={explorer.article} /></Box>)
    }
    return (<Box sx={root}></Box>)
  }, [activeTab, site]);
}
export { Main }


