import React from 'react';
import { useSnackbar } from 'notistack';

import { FormattedMessage, useIntl } from 'react-intl';

import { StencilComposerApi as Composer } from '@dxs-ts/stencil-api';
import { StencilApi } from '@dxs-ts/stencil-api';
import * as Burger from '@dxs-ts/eveli-primitives';
import { Box, Dialog, DialogTitle, DialogContent, DialogActions, Button, FormHelperText } from '@mui/material';

import { useStencilNav } from '../stencil-nav';
import { CancelButton } from '@dxs-ts/eveli-primitives';



const NewPage: React.FC<{ onClose: () => void, articleId?: StencilApi.ArticleId }> = (props) => {
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, site, session } = Composer.useComposer();
  const [locale, setLocale] = React.useState('');
  const [template, setTemplate] = React.useState<StencilApi.TemplateId | ''>('');
  const [articleId, setArticleId] = React.useState(props.articleId ? props.articleId : '');
  const [devMode, setDevMode] = React.useState<boolean>(false);
  const { onNav } = useStencilNav();

  const handleCreate = () => {

    const content = template ? site.templates[template].body.content : undefined;
    const entity: StencilApi.CreatePage = { articleId, locale, content, devMode };
    service.create().page(entity).then(success => {
      enqueueSnackbar(message, { variant: 'success' });
      console.log(success)
      props.onClose();
      actions.handleLoadSite().then(() => {
        const article = site.articles[articleId];
        onNav({ article:  article.id, type: "ARTICLE_PAGES", locale1: locale })
      });

    })
  }
  const message = <FormattedMessage id="snack.page.createdMessage" />
  const definedLocales: StencilApi.LocaleId[] = Object.values(site.pages)
    .filter(p => p.body.article === articleId).map(p => p.body.locale);

  const articles: StencilApi.Article[] = session.articles.map(w => w.article);
  const locales: StencilApi.SiteLocale[] = Object.values(site.locales).filter(l => !definedLocales.includes(l.id));
  const templates: StencilApi.Template[] = Object.values(site.templates);

  return (
    <Dialog open={true} onClose={props.onClose}>
      <DialogTitle><FormattedMessage id='newpage.title' /></DialogTitle>
      <DialogContent>
      <FormattedMessage id='newpage.info' />

        <Burger.Select
          selected={articleId}
          onChange={setArticleId}
          label='article.name'
          items={articles.map((article) => ({
            id: article.id,
            sx: article.body.parentId ? { ml: 2, color: "article.dark" } : undefined,
            value: `${article.body.parentId ? site.articles[article.body.parentId].body.name + "/" : ""}${article.body.name}`
          }))}
        />
        {!articleId && (
          <FormHelperText error>
            {intl.formatMessage({ id: 'error.valueRequired' })}
          </FormHelperText>
        )}
        <Burger.Select
          selected={locale}
          onChange={setLocale}
          label='locale'
          items={locales.map((locale) => ({ id: locale.id, value: locale.body.value }))}
        />
        {!locale && (
          <FormHelperText error>
            {intl.formatMessage({ id: 'error.valueRequired' })}
          </FormHelperText>
        )}
        {templates.length > 0 ?
          <Burger.Select
            selected={template}
            onChange={setTemplate}
            label='newpage.template.select'
            empty={{ id: '', label: 'template.newpage.none' }}
            items={templates.map((template) => ({ id: template.id, value: template.body.name }))}
          />
          : null}
        <Box maxWidth="50%" sx={{ ml: 1 }}>
          <Burger.Switch
            checked={devMode ? devMode : false}
            helperText="pages.devmode.helper"
            label="pages.devmode"
            onChange={setDevMode}
          />
        </Box>
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={props.onClose} />
        <Button onClick={handleCreate} disabled={!locale}>
          <FormattedMessage id='button.create'/>
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export { NewPage }