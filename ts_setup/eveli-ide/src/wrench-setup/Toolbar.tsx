import React from 'react';

import { IconButton, Typography } from '@mui/material';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import DashboardCustomizeOutlinedIcon from '@mui/icons-material/DashboardCustomizeOutlined';
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined';
import SettingsOutlinedIcon from '@mui/icons-material/SettingsOutlined';
import SearchIcon from '@mui/icons-material/Search';
import TaskOutlinedIcon from '@mui/icons-material/TaskOutlined';
import EditNoteOutlinedIcon from '@mui/icons-material/EditNoteOutlined';
import BuildOutlinedIcon from '@mui/icons-material/BuildOutlined';
import ListIcon from '@mui/icons-material/ListAlt';
import { FormattedMessage } from 'react-intl';
import { useSnackbar } from 'notistack';

import { EveliShellMiniBarClassName, EveliShellMiniBarRoot, useUtilityClasses } from '../eveli-shell/useUtilityClasses';
import { useNavigate } from '@tanstack/react-router';
import { useWrenchNav } from '../wrench-nav';

import { EveliLocales } from '@/eveli-locales';
import { WrenchComposerApi } from './ide';



export const Toolbar: React.FC<{}> = () => {
  const navigate = useNavigate();
  const composer = WrenchComposerApi.useComposer();
  const { onNav, activeItem } = useWrenchNav();
  const { enqueueSnackbar } = useSnackbar();

  const classes = useUtilityClasses();

  const unsavedPages = Object.values(composer.session.pages).filter(p => !p.saved);
  const saveIconClassName = unsavedPages.length ? classes.unsaved : classes.itemDisabled;

  const handleSave = (_event: React.SyntheticEvent) => {
    if (!unsavedPages || unsavedPages.length === 0) {
      return;
    }

    if(activeItem?.type === 'ENTITY_EDITOR') {

      const article = composer.session.getEntity(activeItem.id);
      if (!article) {
        return;
      }
      const toBeSaved = unsavedPages.filter(p => !p.saved).filter(p => p.origin.id === article.id);
      if (toBeSaved.length !== 1) {
        return;
      }

      const unsavedArticlePages: WrenchComposerApi.PageUpdate = toBeSaved[0];
      composer.service.update(article.id, unsavedArticlePages.value).then(async success => {
        await composer.actions.handleLoadSite(success)
        composer.actions.handlePageUpdateRemove([article.id]);
        enqueueSnackbar(<FormattedMessage id="activities.assets.saveSuccess" values={{ name: article.ast?.name }} />);  
      }).catch((error) => {

      });
    }
  };


  return (
    <EveliShellMiniBarRoot className={EveliShellMiniBarClassName} ownerState={{ unsaved: unsavedPages.length > 0 }}>

      <div>
        <IconButton onClick={(event) => {}}><SearchIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.search' /></Typography>
      </div>

      <div>
        <IconButton onClick={() => navigate({
          from: '/secured/$locale/assets/wrench',
          to: '/secured/$locale'
        })}>
          <TaskOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.tasks' /></Typography>
      </div>

      <div>
        <IconButton disabled className={classes.itemActive}><BuildOutlinedIcon /></IconButton>
        <Typography className={classes.textActive}><FormattedMessage id='toolbar.wrench' /></Typography>
      </div>

      <div>
        <IconButton onClick={() => navigate({
          from: '/secured/$locale/assets/wrench',
          to: '/secured/$locale/assets/stencil',
          search: { explorer: [] }
        })}>
          <EditNoteOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.stencil' /></Typography>
      </div>
      <div>
        <IconButton onClick={() => navigate({
          from: '/secured/$locale',
          to: '/secured/$locale/assets/services'
        })}>

          <SettingsOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='menu.workflows' /></Typography>
      </div>


      <div>
        <IconButton onClick={() => navigate({
            from: '/secured/$locale',
            to: '/secured/$locale/assets/forms'
          })}>
          <ListIcon />
        </IconButton>
        <Typography><FormattedMessage id='menu.forms' /></Typography>
      </div>

      <div>
        <IconButton onClick={() => window.open("https://github.com/the-stencil-io/the-stencil-composer/wiki", "_blank")}>
          <HelpOutlineOutlinedIcon />
        </IconButton>
        <Typography><FormattedMessage id='toolbar.help' /></Typography>
      </div>

      <EveliLocales />

      <div>
        <IconButton onClick={(event) => onNav({ type: 'ACTIVITIES' })}><DashboardCustomizeOutlinedIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.activities' /></Typography>
      </div>

      <div>
        <IconButton className={saveIconClassName} onClick={handleSave} ><SaveOutlinedIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.save' /></Typography>
      </div>

    </EveliShellMiniBarRoot>
  );
}