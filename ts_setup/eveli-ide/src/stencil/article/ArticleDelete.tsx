import React from 'react';
import { DialogContentText } from '@mui/material';
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
    <Burger.Dialog open={true} onClose={onClose}
      backgroundColor="uiElements.main"
      title="article.delete.title"
      submit={{ title: "button.delete", onClick: handleDelete, disabled: false }}>
      <DialogContentText>
        <FormattedMessage id="article.delete" />
      </DialogContentText>
    </Burger.Dialog>);
};
export { ArticleDelete };
