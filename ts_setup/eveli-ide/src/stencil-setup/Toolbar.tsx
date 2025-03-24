import React from 'react';
import { useSnackbar } from 'notistack';
import { IconButton, Typography } from '@mui/material';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import DashboardCustomizeOutlinedIcon from '@mui/icons-material/DashboardCustomizeOutlined';
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined';
import SearchIcon from '@mui/icons-material/Search';
import TaskOutlinedIcon from '@mui/icons-material/TaskOutlined';
import BuildOutlinedIcon from '@mui/icons-material/BuildOutlined';
import EditNoteOutlinedIcon from '@mui/icons-material/EditNoteOutlined';
import SettingsOutlinedIcon from '@mui/icons-material/SettingsOutlined';
import ListIcon from '@mui/icons-material/ListAlt';
import { FormattedMessage } from 'react-intl';

import { StencilComposerApi } from './ide';

import { StencilApi } from '../api-stencil';
import { EveliShellMiniBarRoot, useUtilityClasses, EveliShellMiniBarClassName } from '../eveli-shell/useUtilityClasses';
import { useNavigate } from '@tanstack/react-router';
import { useStencilNav } from '../stencil-nav';
import { EveliLocales } from '@/eveli-locales';
import { useIconbar } from '@/api-iconbar';


export const Toolbar: React.FC<{}> = () => {
  const { enqueueSnackbar } = useSnackbar();
  const navigate = useNavigate();
  const composer = StencilComposerApi.useComposer();
  const { onNav, activeItem } = useStencilNav();
  const secondaryCtx = useIconbar();

  const classes = useUtilityClasses();
  const article = activeItem?.type === "ARTICLE_PAGES" ? composer.site.articles[activeItem.article] : undefined;
  
  const unsavedPages = Object.values(composer.session.pages).filter(p => !p.saved);
  const unsavedArticlePages: StencilComposerApi.PageUpdate[] = (article ? unsavedPages.filter(p => !p.saved).filter(p => p.origin.body.article === article.id) : []);
  const message = <FormattedMessage id="snack.page.savedMessage" />


  const handleSearch = (_event: React.SyntheticEvent) => {
    secondaryCtx.handleActiveId("toolbar.search")
  };


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

  const saveIconClassName = unsavedPages.length ? classes.unsaved : classes.itemDisabled;

  return (
    <EveliShellMiniBarRoot className={EveliShellMiniBarClassName} ownerState={{ unsaved: unsavedPages.length > 0 }}>
      <div>
        <IconButton onClick={() => onNav({ type: 'ACTIVITIES' })}><DashboardCustomizeOutlinedIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.activities' /></Typography>
      </div>

      <div>
        <IconButton className={saveIconClassName} onClick={handleSave} ><SaveOutlinedIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.save' /></Typography>
      </div>
    </EveliShellMiniBarRoot>
  );
}