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

import { FormattedMessage, useIntl } from 'react-intl';

import * as Burger from '@/burger';
import { Composer, StencilApi } from './context';
import { EveliShellMiniBarRoot, useUtilityClasses, EveliShellMiniBarClassName } from '../burger/eveli-shell/useUtilityClasses';
import { useNavigate } from '@tanstack/react-router';
import { useStencilNav } from './nav';


export const Toolbar: React.FC<{}> = () => {
  const { enqueueSnackbar } = useSnackbar();
  const navigate = useNavigate();
  const composer = Composer.useComposer();
  const tabs = Burger.useTabs();

  const {onNav} = useStencilNav();
  const secondaryCtx = Burger.useIconbar();

  const classes = useUtilityClasses();

  const active = tabs.session.tabs.length ? tabs.session.tabs[tabs.session.history.open] : undefined;
  const article = active ? composer.site.articles[active.id] : undefined;
  const articlePagesView = active?.data?.nav?.type === "ARTICLE_PAGES";
  const unsavedPages = Object.values(composer.session.pages).filter(p => !p.saved);
  const unsavedArticlePages: Composer.PageUpdate[] = (article ? unsavedPages.filter(p => !p.saved).filter(p => p.origin.body.article === article.id) : []);
  const message = <FormattedMessage id="snack.page.savedMessage" />


  const handleChange = (_event: React.SyntheticEvent, newValue: string) => {

    if (newValue === 'toolbar.save' && articlePagesView && article) {
      if (unsavedArticlePages.length === 0) {
        return;
      }
      const update: StencilApi.PageMutator[] = unsavedArticlePages.map(p => ({ pageId: p.origin.id, locale: p.origin.body.locale, content: p.value, devMode: p.origin.body.devMode }));
      composer.service.update().pages(update).then(success => {
        enqueueSnackbar(message, { variant: 'success' });
        composer.actions.handlePageUpdateRemove(success.map(p => p.id));
      }).then(() => {
        composer.actions.handleLoadSite();
      });

    } else if (newValue === 'toolbar.search') {
      secondaryCtx.handleActiveId("toolbar.search")
    }
  };
  const saveIconClassName = unsavedPages.length ? classes.unsaved : classes.itemDisabled;

  return (
    <EveliShellMiniBarRoot className={EveliShellMiniBarClassName} ownerState={{ unsaved: unsavedPages.length > 0 }}>
      <div>
        <IconButton onClick={() => onNav('ACTIVITIES')}><DashboardCustomizeOutlinedIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.activities' /></Typography>
      </div>

      <div>
        <IconButton className={saveIconClassName} onClick={(event) => handleChange(event, 'toolbar.save')} ><SaveOutlinedIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.save' /></Typography>
      </div>

      <div>
        <IconButton onClick={(event) => handleChange(event, 'toolbar.search')}><SearchIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.search' /></Typography>
      </div>

      <div>
        <IconButton onClick={() => navigate({
          from: '/secured/$locale/assets/stencil',
          to: '/secured/$locale'
        })}>
          <TaskOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.tasks' /></Typography>
      </div>

      <div>
        <IconButton onClick={() => navigate({
          from: '/secured/$locale/assets/stencil',
          to: '/secured/$locale/assets/wrench',
          search: { explorer: [] }
        })}>
          <BuildOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.wrench' /></Typography>
      </div>

      <div>
        <IconButton disabled className={classes.itemActive}><EditNoteOutlinedIcon /></IconButton>
        <Typography className={classes.textActive}><FormattedMessage id='toolbar.stencil' /></Typography>
      </div>

      <div>
        <IconButton onClick={() => window.open("https://github.com/the-stencil-io/the-stencil-composer/wiki", "_blank")}>
          <HelpOutlineOutlinedIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.help' /></Typography>
      </div>

      <Burger.EveliLocales />
    </EveliShellMiniBarRoot>
  );
}