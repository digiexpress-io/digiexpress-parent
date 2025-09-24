import React from 'react';
import { useSnackbar } from 'notistack';
import { FormattedMessage } from 'react-intl';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material'

import { StencilComposerApi as Composer } from '@dxs-ts/stencil-api';
import { StencilApi } from '@dxs-ts/stencil-api';
import { useStencilNav } from '../stencil-nav';

import * as Burger from '@dxs-ts/eveli-primitives';
import { CancelButton } from '@dxs-ts/eveli-primitives';
import { parsePageTitle } from './helpers';


const PageDelete: React.FC<{ onClose: () => void, articleId: StencilApi.ArticleId }> = (props) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, site } = Composer.useComposer();
  const [pageId, setPageId] = React.useState('');
  const { onTabClose, findTab } = useStencilNav();

  const handleDelete = () => {
    const pageTab =  findTab('ARTICLE_PAGES', props.articleId)

    service.delete().page(pageId).then(async _success => {
      if (pageTab) {
        onTabClose(pageTab);
      }

      await actions.handleLoadSite();
      enqueueSnackbar(message, { variant: 'warning' });
      props.onClose();
    })
  }

  const message = <FormattedMessage id="snack.page.deletedMessage" />
  const articlePages: StencilApi.Page[] = Object.values(site.pages).filter(p => p.body.article === props.articleId);



  const article = site.articles[props.articleId];
  const articleName = article?.body?.name || '';

  return (
    <Dialog open={true} onClose={props.onClose}>
      <DialogTitle><FormattedMessage id='page.delete.dialog.title' />
        <FormattedMessage id='eveli.textSeparatorColon' />
        {articleName}
      </DialogTitle>
      <DialogContent>
        <FormattedMessage id='page.delete.description' />
        <Burger.Select
          selected={pageId}
          onChange={setPageId}
          label='pages.edit.selectpage'
          items={articlePages.map(articlePage => ({
            id: articlePage.id,
            value: `${site.locales[articlePage.body.locale].body.value}:  ${parsePageTitle(articlePage)}`
          }))}
        />
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={props.onClose} />
        <Button onClick={handleDelete} disabled={!pageId}>
          <FormattedMessage id='button.delete.page' />
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export { PageDelete }