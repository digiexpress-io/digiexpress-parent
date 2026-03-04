import React from 'react';
import { Box, Dialog, DialogTitle, DialogContent, DialogActions, Button, FormHelperText } from '@mui/material';
import { useSnackbar } from 'notistack';

import { FormattedMessage, useIntl } from 'react-intl';

import * as Burger from '@dxs-ts/eveli-primitives';
import { StencilComposerApi as Composer } from '@dxs-ts/stencil-api';
import { StencilApi } from '@dxs-ts/stencil-api';
import { ArticleOrderNumberViewer } from './ArticleOrderNumberViewer';
import { CancelButton } from '@dxs-ts/eveli-primitives';


const selectSub = { ml: 2, color: "article.dark" }

const ArticleEdit: React.FC<{ articleId: StencilApi.ArticleId, onClose: () => void }> = ({ articleId, onClose }) => {
  const intl = useIntl();
  const { service, actions, session } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();

  const { site } = session;
  const article = site.articles[articleId];
  const [name, setName] = React.useState(article.body.name);
  const [order, setOrder] = React.useState(article.body.order);
  const [parentId, setParentId] = React.useState(article.body.parentId);
  const [devMode, setDevMode] = React.useState(article.body.devMode);

  const message = <FormattedMessage id="snack.article.editedMessage" />

  const handleUpdate = () => {
    const entity: StencilApi.ArticleMutator = { articleId: article.id, name: name.trim(), parentId, order, links: undefined, workflows: undefined, devMode };

    service.update().article(entity).then(_success => {
      enqueueSnackbar(message, { variant: 'success' });
      onClose();
      actions.handleLoadSite();
    });
  }

  const articles: { id: string, value: string }[] = Object.values(site.articles)
    .sort((a1, a2) => {
      if (a1.body.parentId && a1.body.parentId === a2.body.parentId) {
        const children = a1.body.order - a2.body.order;
        if (children === 0) {
          return a1.body.name.localeCompare(a2.body.name);
        }
        return children;
      }

      return (a1.body.parentId ? site.articles[a1.body.parentId].body.order + 1 : a1.body.order)
        - (a2.body.parentId ? site.articles[a2.body.parentId].body.order + 1 : a2.body.order);
    })
    .map(article => ({
      id: article.id,
      value: `${article.body.order} - ${article.body.parentId ? site.articles[article.body.parentId].body.name + "/" : ""}${article.body.name}`,
      sx: article.body.parentId ? selectSub : undefined
    }));

  return (
  <Dialog open={true} onClose={onClose}>
    <DialogTitle><FormattedMessage id='article.edit.title'/></DialogTitle>
    <DialogContent>
      <Burger.Select label="article.edit.parent" onChange={setParentId}
        selected={parentId ? parentId : ''}
        items={articles}
        empty={{ id: "", label: "article.composer.parent.unselected" }}
      />

      <Box display='flex' alignItems='center'>
        <Box>
          <Burger.NumberField label="article.order" helperText='article.composer.orderhelper'
            onChange={setOrder}
            value={order}
            placeholder={400}
          />
            {!order && <FormHelperText error>{intl.formatMessage({ id: 'error.valueRequired' })}</FormHelperText>}
        </Box>
        <Box sx={{ width: '10%' }}>
          <ArticleOrderNumberViewer />
        </Box>
      </Box>

      <Burger.TextField label="article.name" required value={name} onChange={setName} />
        {!name.trim() && <FormHelperText error>{intl.formatMessage({ id: 'error.valueRequired' })}</FormHelperText>}

      <Box maxWidth="50%" sx={{ ml: 1 }}>
        <Burger.Switch
          checked={devMode ? devMode : false}
          helperText="article.devmode.helper"
          label="article.devmode"
          onChange={setDevMode}
        />
      </Box>
    </DialogContent>
    <DialogActions>
      <CancelButton onClick={onClose} />
        <Button onClick={handleUpdate} disabled={!name.trim() || !order}>
        <FormattedMessage id='button.update' />
      </Button>
    </DialogActions>
  </Dialog>);
}

export { ArticleEdit }


