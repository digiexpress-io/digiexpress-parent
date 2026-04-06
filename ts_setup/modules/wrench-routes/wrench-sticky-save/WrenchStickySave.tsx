import React from 'react';

import { Button, Typography, lighten, useTheme } from '@mui/material';
import { SaveOutlined as SaveOutlinedIcon } from '@mui/icons-material';
import { FormattedMessage, useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';

import { WrenchComposerApi } from '@dxs-ts/wrench-api';
import { useWrenchNav } from '../wrench-nav';



function useSave() {
  const { enqueueSnackbar } = useSnackbar();
  const composer = WrenchComposerApi.useComposer();
  const { activeItem } = useWrenchNav();

  if (activeItem?.type !== 'ENTITY_EDITOR') {
    return { enabled: false, onSave: () => { } }
  }
  const entity = composer.session.getEntity(activeItem.id);
  if (!entity) {
    return { enabled: false, onSave: () => { } }
  }

  const unsaved = Object.values(composer.session.pages)
    .filter(p => !p.saved)
    .find(p => p.origin.id === entity.id);

  const enabled = !!unsaved;

  function onSave() {
    if (!enabled || !entity) {
      return;
    }
    const unsavedArticlePages: WrenchComposerApi.PageUpdate = unsaved;
    composer.service.update(entity.id, entity.ast.bodyType, unsavedArticlePages.value).then(async success => {
      await composer.actions.handleLoadSite(success)
      composer.actions.handlePageUpdateRemove([entity.id]);
      enqueueSnackbar(
        <FormattedMessage id="activities.assets.saveSuccess" values={{ name: entity.ast?.name }} />,
        { variant: 'success' }
      );      
    }).catch((_error) => {

    });
  }
  return { enabled, onSave }
}


export const WrenchStickySave: React.FC<{}> = ({ }) => {
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
        top: 15,
        right: 16,
        zIndex: 1100,
        position: 'fixed',
        padding: theme.spacing(2),
        backgroundColor: theme.palette.warning.main,
        color: theme.palette.text.primary,
        animation: 'pulse 1.5s ease-in-out infinite',
        transition: 'transform 0.3s ease-in-out',
        '@keyframes pulse': {
          '0%': { transform: 'scale(1)', opacity: 1 },
          '50%': { transform: 'scale(1.1)', opacity: 0.8 },
          '100%': { transform: 'scale(1)', opacity: 1 },
        },
        ':hover': {
          backgroundColor: lighten(theme.palette.warning.main, 0.2),
        }
      }}>
      <Typography fontWeight='bold'>{intl.formatMessage({ id: 'toolbar.save' })}</Typography>
    </Button>
  )
}

/*
'& .EveliShell-unsaved': {
  color: ownerState.unsaved ? theme.palette.common.black : theme.palette.text.secondary,
  backgroundColor: alpha(theme.palette.warning.main, 0.8),
  padding: theme.spacing(1)
}*/