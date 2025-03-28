import React from 'react';
import { Button, lighten, Typography, useTheme } from '@mui/material';
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined';

import { FormattedMessage, useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';

import { useStencilNav } from '@/stencil-nav';
import { StencilComposerApi } from '@/stencil-setup';
import { StencilApi } from '@/api-stencil';


function useSave() {
  const { enqueueSnackbar } = useSnackbar();
  const composer = StencilComposerApi.useComposer();
  const { activeItem } = useStencilNav();

  if (activeItem?.type !== 'ARTICLE_PAGES') {
    return { enabled: false, onSave: () => { } }
  }

  const article = activeItem?.type === "ARTICLE_PAGES" ? composer.site.articles[activeItem.article] : undefined;
  const unsavedPages = Object.values(composer.session.pages).filter(p => !p.saved);
  const unsavedArticlePages: StencilComposerApi.PageUpdate[] = (article ? unsavedPages.filter(p => !p.saved).filter(p => p.origin.body.article === article.id) : []);

  const enabled = unsavedArticlePages.length > 0;
  const message = <FormattedMessage id="snack.page.savedMessage" />


  const onSave = (_event: React.SyntheticEvent) => {
    if (!enabled || !article) {
      return;
    }

    if (article) {
      if (unsavedArticlePages.length === 0) {
        return;
      }
      const update: StencilApi.PageMutator[] = unsavedArticlePages
        .map(p => ({ pageId: p.origin.id, locale: p.origin.body.locale, content: p.value, devMode: p.origin.body.devMode }));

      composer.service.update().pages(update).then(success => {
        return composer.actions.handleLoadSite().then(() => {
          enqueueSnackbar(message, { variant: 'success' });
          composer.actions.handlePageUpdateRemove(success.map(p => p.id));
        })
      });
    }
  }
  return { enabled, onSave }
};



export const StencilStickySave: React.FC = () => {
  const intl = useIntl();
  const theme = useTheme();
  const { enabled, onSave } = useSave();

  if (!enabled) {
    return <></>;
  }

  return (
    <Button startIcon={<SaveOutlinedIcon fontSize='inherit' />}
      onClick={onSave}
      sx={{
        top: 80,
        right: 16,
        zIndex: 1100,
        position: 'fixed',
        padding: theme.spacing(2),
        backgroundColor: theme.palette.warning.main,
        color: theme.palette.text.primary,
        ':hover': {
          backgroundColor: lighten(theme.palette.warning.main, 0.2),
        }
      }}>
      <Typography fontWeight='bold'>{intl.formatMessage({ id: 'toolbar.save' })}</Typography>
    </Button>
  )
}