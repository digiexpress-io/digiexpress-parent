import React from 'react';
import { useSnackbar } from 'notistack';
import { FormattedMessage } from 'react-intl';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material'

import { Composer, StencilApi } from '../context';
import * as Burger from '@/burger';


const PageDelete: React.FC<{ onClose: () => void, articleId: StencilApi.ArticleId }> = (props) => {
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
  const articlePages: StencilApi.Page[] = Object.values(site.pages).filter(p => p.body.article === props.articleId);

  return (
    <Dialog open={true} onClose={props.onClose}>
      <DialogTitle><FormattedMessage id='pages.delete' /></DialogTitle>
      <DialogContent>
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
      </DialogContent>
      <DialogActions>
        <Button variant='text' onClick={props.onClose}>
          <FormattedMessage id='button.cancel'/>
        </Button>
        <Button onClick={handleDelete} disabled={!pageId}>
          <FormattedMessage id='button.delete'/>
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export { PageDelete }