import React from 'react';
import { Box, Dialog, DialogTitle, DialogContent, DialogActions, Button, FormHelperText } from '@mui/material';
import { useSnackbar } from 'notistack';

import * as Burger from '@dxs-ts/eveli-primitives';

import { StencilComposerApi as Composer } from '@dxs-ts/stencil-api';
import { StencilApi } from '@dxs-ts/stencil-api';
import { ArticleOrderNumberViewer } from './ArticleOrderNumberViewer';

import { FormattedMessage, useIntl } from 'react-intl';
import { CancelButton } from '@dxs-ts/eveli-primitives';

const DUMMY_ID = "none-selected"


const ArticleComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const intl = useIntl();
  const { service, actions, session } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState("");
  const [order, setOrder] = React.useState(0);
  const [parentId, setParentId] = React.useState("");
  const [devMode, setDevMode] = React.useState<boolean>(false);
  const message = <FormattedMessage id="snack.article.createdMessage" values={{ name }} />

  const handleCreate = () => {
    const entity: StencilApi.CreateArticle = { name: name.trim(), parentId: parentId && parentId !== DUMMY_ID ? parentId : undefined, order, devMode };

    service.create().article(entity).then(success => {
      console.log(success)
      enqueueSnackbar(message, { variant: 'success' });
      onClose();
      actions.handleLoadSite();
    });
  }


  return (
    <Dialog open={true} onClose={onClose}>
    <DialogTitle><FormattedMessage id='article.composer.title'/></DialogTitle>
    <DialogContent>
      <Burger.Select label="article.composer.parent"
        helperText={"article.parent.helper"}
        selected={parentId}
        onChange={setParentId}
        empty={{ id: DUMMY_ID, label: 'article.composer.parent.unselected' }}
        items={session.articles
          .map(view => view.article)
          .map(({ id, body }) => ({
            id,
            value: (<Box sx={body.parentId ? { ml: 2, color: 'primary.main' } : undefined}>{`${body.order} - ${body.name}`}</Box>)
          }))}
      />
      <Box display='flex' alignItems='center'>
        <Box>
          <Burger.NumberField label="article.order" helperText='article.composer.orderhelper'
            onChange={setOrder}
            value={order}
            placeholder={400}
          />
        </Box>
        <Box sx={{ width: '10%' }}>
          <ArticleOrderNumberViewer />
        </Box>
        </Box>
        <Burger.TextField
          label="article.name"
          required
          value={name}
          onChange={setName}
        />
        {!name.trim() && (
          <FormHelperText error>
            {intl.formatMessage({ id: 'error.valueRequired' })}
          </FormHelperText>
        )}
        <Box maxWidth="50%" sx={{ ml: 1 }}>
        <Burger.Switch
          checked={devMode}
          helperText="article.devmode.helper"
          label="article.devmode"
          onChange={setDevMode}
        />
      </Box>
    </DialogContent>
    <DialogActions>
      <CancelButton onClick={onClose} />
        <Button onClick={handleCreate} disabled={!name.trim()}>
        <FormattedMessage id='article.create'/>
      </Button>
    </DialogActions>
  </Dialog>
  );
}

export { ArticleComposer }