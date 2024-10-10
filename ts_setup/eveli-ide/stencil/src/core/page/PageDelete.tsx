import React from 'react';
import { useSnackbar } from 'notistack';
import { FormattedMessage } from 'react-intl';

import { Composer, StencilClient } from '../context';
import Burger from '@the-wrench-io/react-burger';


const PageDelete: React.FC<{ onClose: () => void, articleId: StencilClient.ArticleId }> = (props) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, site } = Composer.useComposer();
  const [pageId, setPageId] = React.useState('');
  const tabs = Burger.useTabs();

  const handleDelete = () => {
    var pageTab = tabs.session.tabs.find(tab => tab.id === props.articleId)
    service.delete().page(pageId).then(_success => {
      if (pageTab) {
        tabs.actions.handleTabClose(pageTab);
      }
      enqueueSnackbar(message, { variant: 'warning' });
      props.onClose();
      actions.handleLoadSite();
    })
  }

  const message = <FormattedMessage id="snack.page.deletedMessage" />
  const articlePages: StencilClient.Page[] = Object.values(site.pages).filter(p => p.body.article === props.articleId);
  return (
    <Burger.Dialog open={true} onClose={props.onClose}
      backgroundColor="uiElements.main"
      title="pages.delete"
      submit={{ title: "button.delete", onClick: handleDelete, disabled: !pageId }}>
      <>
        <FormattedMessage id='pages.delete.message' />
        <Burger.Select
          selected={pageId}
          onChange={setPageId}
          label='pages.edit.selectpage'
          items={articlePages.map(articlePage => ({
            id: articlePage.id,
            value: site.locales[articlePage.body.locale].body.value
          }))}
        />
      </>
    </Burger.Dialog>
  );
}

export { PageDelete }