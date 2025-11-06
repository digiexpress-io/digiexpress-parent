import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography, Box } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';
import { ArticleDeleteRoot, useArticleDeleteUtilityClasses } from './useUtilityClasses';

import { StencilComposerApi as Composer } from '@dxs-ts/stencil-api';
import { StencilApi } from '@dxs-ts/stencil-api';
import { CancelButton } from '@dxs-ts/eveli-primitives';

interface ArticleDeleteProps {
  articleId: StencilApi.ArticleId;
  onClose: () => void;
}

const ArticleDelete: React.FC<ArticleDeleteProps> = ({ articleId, onClose }) => {
  const intl = useIntl();
  const classes = useArticleDeleteUtilityClasses();
  const { service, actions, site } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();

  const article = site?.articles?.[articleId];
  const articleName = article?.body?.name ?? intl.formatMessage({ id: 'article.unknown' });

  const message = <FormattedMessage id="snack.article.deletedMessage" />;

  const handleDelete = () => {
    service.delete().article(articleId).then(_success => {
      enqueueSnackbar(message, { variant: 'warning' });
      onClose();
      actions.handleLoadSite();
    });
  };

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>
        <FormattedMessage id='article.delete.title' />
      </DialogTitle>

      <DialogContent>
        <Typography className={classes.description}>
          <FormattedMessage id='article.delete.description' />
        </Typography>
               <ArticleDeleteRoot className={classes.root}>
          <Box className={classes.infoBox}>
            <Typography variant="body2" component="div">
              <strong className={classes.label}>
                {intl.formatMessage({ id: 'article.name', defaultMessage: 'Name' })}:
              </strong>{' '}
              <span className={classes.value}>{articleName}</span>
            </Typography>
          </Box>
        </ArticleDeleteRoot>
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleDelete} color="error">
          <FormattedMessage id='button.delete.article' />
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export { ArticleDelete };
