import React from 'react';
import { useSnackbar } from 'notistack';
import { FormattedMessage } from 'react-intl';
import Burger from '@the-wrench-io/react-burger';

import { Composer, StencilClient } from '../context';



const ArticleLinksComposer: React.FC<{ articleId: StencilClient.ArticleId }> = (props) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, site, session } = Composer.useComposer();
  const tabs = Burger.useTabs();
  const view = session.getArticleView(props.articleId);

  const links: StencilClient.Link[] = Object.values(site.links)
    .map((w) => ({ w, name: session.getLinkName(w.id)?.name }))
    .sort((a, b) => a.name.localeCompare(b.name))
    .map((w) => w.w);

  const handleSave = (selectedLinks: string[]) => {
    const article = site.articles[props.articleId]
    const entity: StencilClient.ArticleMutator = {
      articleId: article.id,
      name: article.body.name,
      parentId: article.body.parentId,
      order: article.body.order,
      links: [...selectedLinks],
      workflows: undefined,
      devMode: article.body.devMode
    };
    service.update().article(entity)
      .then(_success => actions.handleLoadSite())
      .then(() => tabs.actions.handleTabCloseCurrent())
    enqueueSnackbar(message, { variant: 'success' });
  }
  const message = <FormattedMessage id="snack.link.editedMessage" />
  const articleName = site.articles[props.articleId].body.name;

  return (
    <>
      <Burger.TransferList
        title="articlelinks"
        titleArgs={{ name: articleName }}
        searchTitle="link.search.title"
        selectedTitle="article.links.selectedlinks"
        headers={["link.value", "link.type"]}
        rows={links.map(row => row.id)}
        filterRow={(row, search) => {
          const link = site.links[row];
          return link.body.value.toLowerCase().indexOf(search) > -1;
        }}
        renderCells={(row) => [session.getLinkName(site.links[row].id)?.name, site.links[row].body.contentType]}
        selected={view.links.map(l => l.link.id)}
        cancel={{
          label: 'button.cancel',
          onClick: () => tabs.actions.handleTabCloseCurrent()
        }}
        submit={{
          label: "button.apply",
          disabled: false,
          onClick: handleSave
        }}
      />
    </>
  );
}

export { ArticleLinksComposer }
