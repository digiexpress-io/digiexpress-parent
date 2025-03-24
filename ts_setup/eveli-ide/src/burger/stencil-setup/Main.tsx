import React from 'react';
import { Box } from '@mui/material';


import { ArticlesView, ArticlePageComposer, ArticleWorkflowsComposer, ArticleLinksComposer } from '../stencil-article';
import { WorkflowsView } from '../stencil-workflow';
import { LocalesView } from '../stencil-locale';
import { TemplatesView } from '../stencil-template';
import { LinksView } from '../stencil-link';
import { ExplorerItem, useStencilNav } from '../stencil-nav';
import { SearchView } from '../stencil-search';
import { ReleasesView } from '../stencil-release';

import { Activities } from './Activities';



import { StencilComposerApi as Composer } from './ide';

//TODO == remove this
const root = { height: `100%`, padding: 1, backgroundColor: "primary.contrastText" };

const Main: React.FC<{}> = () => {
  const site = Composer.useSite();
  const { activeItem } = useStencilNav();

  return React.useMemo(() => {
    if (site.contentType === "NO_CONNECTION") {
      return (<Box>{site.contentType}</Box>);
    }
    if (!activeItem) {
      return (<Box sx={root}></Box>)
    }
    const explorer: ExplorerItem | undefined = activeItem;
    if(!explorer) {
      return (<Box sx={root}></Box>)
    }

    switch(explorer.type) {
      case 'RELEASES': return (<Box sx={root}><ReleasesView /></Box>);
      case 'ACTIVITIES': return (<Box sx={root}><Activities /></Box>);
      case 'LOCALES': return (<Box sx={root}><LocalesView /></Box>);
      case 'SERVICES': return (<Box sx={root}><WorkflowsView /></Box>);
      case 'SEARCH': return (<Box sx={root}><SearchView /></Box>);
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
  }, [activeItem, site]);
}
export { Main }


