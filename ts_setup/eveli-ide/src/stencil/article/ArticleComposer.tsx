import React from 'react';
import { Box } from '@mui/material';
import { useSnackbar } from 'notistack';

import * as Burger from '@/burger';

import { Composer, StencilApi } from '../context';
import { ArticleOrderNumberViewer } from './ArticleOrderNumberViewer';

import { FormattedMessage } from 'react-intl';

const DUMMY_ID = "none-selected"




const ArticleComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {

  const { service, actions, session } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const [name, setName] = React.useState("");
  const [order, setOrder] = React.useState(0);
  const [parentId, setParentId] = React.useState("");
  const [devMode, setDevMode] = React.useState<boolean>(false);

  const handleCreate = () => {
    const entity: StencilApi.CreateArticle = { name, parentId: parentId && parentId !== DUMMY_ID ? parentId : undefined, order, devMode };

    service.create().article(entity).then(success => {
      console.log(success)
      enqueueSnackbar(message, { variant: 'success' });
      onClose();
      actions.handleLoadSite();
    });
  }

  const message = <FormattedMessage id="snack.article.createdMessage" values={{ name }} />

  return (
    <Burger.Dialog open={true} onClose={onClose}
      backgroundColor="uiElements.main" title="article.composer.title"
      submit={{ title: "article.create", onClick: handleCreate, disabled: !name }}>
      <>

        <Burger.Select label="article.composer.parent"
          helperText={"article.parent.helper"}
          selected={parentId}
          onChange={setParentId}
          empty={{ id: DUMMY_ID, label: 'article.composer.parent.unselected' }}
          items={session.articles
            .map(view => view.article)
            .map(({ id, body }) => ({
              id,
              value: (<Box sx={body.parentId ? { ml: 2, color: 'article.dark' } : undefined}>{`${body.order} - ${body.name}`}</Box>)
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
        <Burger.TextField label="article.name" required
          value={name}
          onChange={setName}
        />
        <Box maxWidth="50%" sx={{ ml: 1 }}>
          <Burger.Switch
            checked={devMode}
            helperText="article.devmode.helper"
            label="article.devmode"
            onChange={setDevMode}
          />
        </Box>
      </>
    </Burger.Dialog>
  );
}

export { ArticleComposer }