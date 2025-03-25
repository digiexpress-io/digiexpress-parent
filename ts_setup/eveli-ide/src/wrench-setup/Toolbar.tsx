import React from 'react';

import { IconButton, Typography } from '@mui/material';
import DashboardCustomizeOutlinedIcon from '@mui/icons-material/DashboardCustomizeOutlined';
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined';
import { FormattedMessage } from 'react-intl';
import { useSnackbar } from 'notistack';

import { EveliShellMiniBarClassName, EveliShellMiniBarRoot, useUtilityClasses } from '../eveli-shell/useUtilityClasses';
import { useWrenchNav } from '../wrench-nav';

import { WrenchComposerApi } from './ide';



export const Toolbar: React.FC<{}> = () => {
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
      }).catch((_error) => {

      });
    }
  };


  return (
    <EveliShellMiniBarRoot className={EveliShellMiniBarClassName} ownerState={{ unsaved: unsavedPages.length > 0 }}>
      <div>
        <IconButton onClick={(_event) => onNav({ type: 'ACTIVITIES' })}><DashboardCustomizeOutlinedIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.activities' /></Typography>
      </div>

      <div>
        <IconButton className={saveIconClassName} onClick={handleSave} ><SaveOutlinedIcon /></IconButton>
        <Typography><FormattedMessage id='toolbar.save' /></Typography>
      </div>
    </EveliShellMiniBarRoot>
  );
}