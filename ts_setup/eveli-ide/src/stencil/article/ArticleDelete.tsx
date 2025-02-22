import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { useSnackbar } from 'notistack';

import { Composer, StencilApi } from '../context';
import * as Burger from '@/burger';


interface ArticleDeleteProps {
  articleId: StencilApi.ArticleId;
  onClose: () => void;
}

const ArticleDelete: React.FC<ArticleDeleteProps> = ({ articleId, onClose }) => {
  const { service, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const message = <FormattedMessage id="snack.article.deletedMessage" />

  const handleDelete = () => {
    service.delete().article(articleId).then(_success => {
      enqueueSnackbar(message, {variant: 'warning'});
      onClose();
      actions.handleLoadSite();
    });
  }
  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='article.delete.title' /></DialogTitle>
      <DialogContent></DialogContent>
      <DialogActions>
        <Button variant='text' onClick={onClose}>
          <FormattedMessage id='button.cancel'/>
        </Button>
        <Button onClick={handleDelete} >
          <FormattedMessage id='button.delete'/>
        </Button>
      </DialogActions>
    </Dialog>
  );
};
export { ArticleDelete };
