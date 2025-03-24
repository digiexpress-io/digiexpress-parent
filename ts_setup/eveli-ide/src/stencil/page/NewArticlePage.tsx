import React from 'react';
import { Typography, Box,  Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { useSnackbar } from 'notistack';

import { FormattedMessage } from 'react-intl';

import { Composer } from '../context';
import { StencilApi } from '@/burger';
import * as Burger from '@/burger';


interface NewArticlePageProps {
  article: StencilApi.Article,
  open?: StencilApi.SiteLocale,

  onClose: () => void,
  onCreate: (page: StencilApi.Page) => void
}

const NewArticlePage: React.FC<NewArticlePageProps> = ({ article, open, onClose, onCreate }) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, site } = Composer.useComposer();
  const [template, setTemplate] = React.useState<StencilApi.TemplateId | ''>('');
  const [devMode, setDevMode] = React.useState<boolean>(true);

  if (!open) {
    return null;
  }

  const handleCreate = () => {
    // const content = template ? site.templates[template].body.content : undefined;
    const entity: StencilApi.CreatePage = { articleId: article.id, locale: open.id, devMode };
    service.create().page(entity)
      .then(success => actions.handleLoadSite().then(() => success))
      .then(success => {
        onCreate(success);
        onClose();
      })
    enqueueSnackbar(message, { variant: 'success' });

  }
  const message = <FormattedMessage id="snack.page.createdMessage" />


  const articleName = site.articles[article.id].body.name;
  const templates: StencilApi.Template[] = Object.values(site.templates);

  return (
    <Dialog open={open ? true : false} onClose={onClose}>
    <DialogTitle><FormattedMessage id='newpage.title' values={{ name: articleName }}/></DialogTitle>
    <DialogContent>
        <Typography>
          <FormattedMessage id='newpage.article.info' values={{ article: article.body.name, locale: open.body.value }} />
        </Typography>
        {templates.length > 0 ?
          <Burger.Select
            selected={template}
            onChange={setTemplate}
            label='template'
            empty={{ id: '', label: 'newpage.template.none' }}
            items={templates.map((template) => ({ id: template.id, value: template.body.name }))}
          />
          : null}
        <Box maxWidth="50%" sx={{ ml: 1 }}>
          <Burger.Switch
            checked={devMode ? devMode : false}
            helperText="services.devmode.helper"
            label="services.devmode"
            onChange={setDevMode}
          />
        </Box>
      </DialogContent>
      <DialogActions>
        <Button variant='text' onClick={onClose}>
          <FormattedMessage id='button.cancel'/>
        </Button>
        <Button onClick={handleCreate}>
          <FormattedMessage id='button.create'/>
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export { NewArticlePage }