import React from 'react';
import { useSnackbar } from 'notistack';
import { IconButton, Typography } from '@mui/material';
import DashboardCustomizeOutlinedIcon from '@mui/icons-material/DashboardCustomizeOutlined';
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined';
import { FormattedMessage } from 'react-intl';

import { StencilComposerApi } from './ide';

import { StencilApi } from '../api-stencil';
import { EveliShellMiniBarRoot, useUtilityClasses, EveliShellMiniBarClassName } from '../eveli-shell/useUtilityClasses';
import { useStencilNav } from '../stencil-nav';


export const Toolbar: React.FC<{}> = () => {
  const { enqueueSnackbar } = useSnackbar();
  const composer = StencilComposerApi.useComposer();
  const { onNav, activeItem } = useStencilNav();

  const classes = useUtilityClasses();
  const article = activeItem?.type === "ARTICLE_PAGES" ? composer.site.articles[activeItem.article] : undefined;
  
  const unsavedPages = Object.values(composer.session.pages).filter(p => !p.saved);
  const unsavedArticlePages: StencilComposerApi.PageUpdate[] = (article ? unsavedPages.filter(p => !p.saved).filter(p => p.origin.body.article === article.id) : []);
  const message = <FormattedMessage id="snack.page.savedMessage" />


  const handleSave = (_event: React.SyntheticEvent) => {
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
  };


  return (
    <EveliShellMiniBarRoot className={EveliShellMiniBarClassName}>
      <div>
        <IconButton onClick={() => onNav({ type: 'ACTIVITIES' })}><DashboardCustomizeOutlinedIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.activities' /></Typography>
      </div>
    </EveliShellMiniBarRoot>
  );
}