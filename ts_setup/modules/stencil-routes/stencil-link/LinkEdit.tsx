import React from 'react';
import { ListItemText, Box, Typography, Button, Checkbox, Dialog, DialogTitle, DialogContent, DialogActions, useTheme, Divider } from '@mui/material';
import { useSnackbar } from 'notistack';

import { FormattedMessage } from 'react-intl';

import * as Burger from '@dxs-ts/eveli-primitives';
import { StencilComposerApi as Composer } from '@dxs-ts/stencil-api';
import { StencilApi } from '@dxs-ts/stencil-api';
import { LocaleLabels } from '../stencil-locale';
import { CancelButton } from '@dxs-ts/eveli-primitives';

const selectSub = { ml: 2, color: "article.dark" }

const linkTypes: StencilApi.LinkType[] = ["internal", "external", "phone"];

interface LinkEditProps {
  linkId: StencilApi.LinkId,
  onClose: () => void,
}

const LinkEdit: React.FC<LinkEditProps> = ({ linkId, onClose }) => {
  const theme = useTheme();
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, site } = Composer.useComposer();
  const link = site.links[linkId];

  const [value, setValue] = React.useState(link.body.value);
  const [labels, setLabels] = React.useState(link.body.labels);
  const [changeInProgress, setChangeInProgress] = React.useState(false);
  const [contentType, setContentType] = React.useState(link.body.contentType);

  const [articleId, setArticleId] = React.useState<StencilApi.ArticleId[]>(link.body.articles);
  //const locales = labels.map(l => l.locale);
  //const articles: StencilApi.Article[] = locales ? session.getArticlesForLocales(locales) : Object.values(site.articles);

  const [devMode, setDevMode] = React.useState(link.body.devMode);


  const handleUpdate = () => {
    const entity: StencilApi.LinkMutator = { linkId: link.id, type: contentType, articles: articleId, labels, value, devMode };
    console.log("entity", entity)
    service.update().link(entity).then(success => {
      enqueueSnackbar(message, { variant: 'success' });
      console.log(success)
      onClose();
      actions.handleLoadSite();
    });
  }
  const message = <FormattedMessage id="snack.link.editedMessage" />

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
      <DialogTitle><FormattedMessage id='link.edit.title' /></DialogTitle>
      <DialogContent>
        <LocaleLabels
          onChange={(labels) => { setChangeInProgress(false); setLabels(labels.map(l => ({ locale: l.locale, labelValue: l.value }))); }}
          onChangeStart={() => setChangeInProgress(true)}
          selected={labels.map(label => ({ locale: label.locale, value: label.labelValue }))} />

        <Burger.Select label="link.type"
          selected={contentType}
          onChange={setContentType as any}
          items={linkTypes.map(link => ({ id: link, value: link }))}
        />

        <Burger.TextField label="link.content" helperText="link.composer.valuehelper" placeholder={link.body.value}
          required
          value={value}
          onChange={setValue} />

        <Divider sx={{ my: theme.spacing(2) }} />

        <Typography fontWeight='bold'><FormattedMessage id='composer.select.article' /></Typography>
        <Burger.SelectMultiple label='link.article.select' multiline
          variant='ARTICLE_SELECT'
          selected={articleId}
          onChange={setArticleId}
          renderValue={(selected: StencilApi.ArticleId[]) => selected.map((articleId, index) => <div key={index}>{site.articles[articleId].body.name}</div>)}
          items={articles.map((article) => ({
            id: article.id,
            value: (<>
              <Checkbox checked={articleId.indexOf(article.id) > -1} />
              <ListItemText primary={article.value} />
            </>)
          }
          ))}
        />

        <Box maxWidth="50%" sx={{ ml: 1 }}>
          <Burger.Switch
            checked={devMode ? devMode : false}
            helperText="link.devmode.helper"
            label="link.devmode"
            onChange={setDevMode}
          />
        </Box>
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleUpdate} disabled={!value || changeInProgress || labels.length < 1}>
          <FormattedMessage id='button.update' />
        </Button>
      </DialogActions>
    </Dialog>
  );
}
export { LinkEdit }
