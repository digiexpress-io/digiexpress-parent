import React from 'react';
import { useSnackbar } from 'notistack';

import { FormattedMessage } from 'react-intl';

import { Composer, StencilClient } from '../context';
import Burger from '@the-wrench-io/react-burger';
import { Box } from '@mui/material';



const NewPage: React.FC<{ onClose: () => void, articleId?: StencilClient.ArticleId }> = (props) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, site, session } = Composer.useComposer();
  const [locale, setLocale] = React.useState('');
  const [template, setTemplate] = React.useState<StencilClient.TemplateId | ''>('');
  const [articleId, setArticleId] = React.useState(props.articleId ? props.articleId : '');
  const [devMode, setDevMode] = React.useState<boolean>(false);
  const { handleInTab } = Composer.useNav();

  const handleCreate = () => {

    const content = template ? site.templates[template].body.content : undefined;
    const entity: StencilClient.CreatePage = { articleId, locale, content, devMode };
    service.create().page(entity).then(success => {
      enqueueSnackbar(message, { variant: 'success' });
      console.log(success)
      props.onClose();
      actions.handleLoadSite().then(() => {
        const article = site.articles[articleId];
        handleInTab({ article, type: "ARTICLE_PAGES", locale })
      });

    })
  }
  const message = <FormattedMessage id="snack.page.createdMessage" />
  const definedLocales: StencilClient.LocaleId[] = Object.values(site.pages)
    .filter(p => p.body.article === articleId).map(p => p.body.locale);

  const articles: StencilClient.Article[] = session.articles.map(w => w.article);
  const locales: StencilClient.SiteLocale[] = Object.values(site.locales).filter(l => !definedLocales.includes(l.id));
  const templates: StencilClient.Template[] = Object.values(site.templates);

  return (
    <Burger.Dialog open={true} onClose={props.onClose}
      backgroundColor="uiElements.main"
      title="newpage.title"
      //titleArgs={{articleId ? name: articleName : undefined}}
      submit={{ title: "button.create", onClick: handleCreate, disabled: !locale }}>
      <>
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
        <Burger.Select
          selected={locale}
          onChange={setLocale}
          label='locale'
          items={locales.map((locale) => ({ id: locale.id, value: locale.body.value }))}
        />
        {templates.length > 0 ?
          <Burger.Select
            selected={template}
            onChange={setTemplate}
            label='template'
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
      </>
    </Burger.Dialog>
  );
}

export { NewPage }