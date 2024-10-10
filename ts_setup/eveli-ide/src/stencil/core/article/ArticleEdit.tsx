import React from 'react';
import { useSnackbar } from 'notistack';
import { FormattedMessage } from 'react-intl';

import Burger from '@the-wrench-io/react-burger';
import { Composer, StencilClient } from '../context';
import { Box } from '@mui/material';


const selectSub = { ml: 2, color: "article.dark" }

const ArticleEdit: React.FC<{ articleId: StencilClient.ArticleId, onClose: () => void }> = ({ articleId, onClose }) => {
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
    const entity: StencilClient.ArticleMutator = { articleId: article.id, name, parentId, order, links: undefined, workflows: undefined, devMode };
    service.update().article(entity).then(_success => {
      enqueueSnackbar(message, {variant: 'success'});
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

  return (<Burger.Dialog open={true} onClose={onClose}
    backgroundColor="uiElements.main"
    title="article.edit.title"
    submit={{ title: "button.update", onClick: handleUpdate, disabled: !name }}>
    <>
      <Burger.Select label="article.edit.parent" onChange={setParentId}
        selected={parentId ? parentId : ''}
        items={articles}
        empty={{ id: "", label: "article.composer.parent.unselected" }}
      />
      <Burger.NumberField label="order" helperText="article.edit.orderhelper" placeholder={100} value={order} onChange={setOrder} />
      <Burger.TextField label="article.name" required value={name} onChange={setName} />
      <Box maxWidth="50%" sx={{ ml: 1 }}>
        <Burger.Switch
          checked={devMode ? devMode : false}
          helperText="article.devmode.helper"
          label="article.devmode"
          onChange={setDevMode}
        />
      </Box>
    </>
  </Burger.Dialog>);
}

export { ArticleEdit }


