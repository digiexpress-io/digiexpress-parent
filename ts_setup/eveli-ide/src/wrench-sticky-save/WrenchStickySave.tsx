import React from 'react';

import { Button, Typography, lighten, useTheme } from '@mui/material';
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined';
import { FormattedMessage, useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';

import { WrenchComposerApi } from '@/wrench-setup';
import { useWrenchNav } from '@/wrench-nav';



function useSave() {
  const { enqueueSnackbar } = useSnackbar();
  const composer = WrenchComposerApi.useComposer();
  const { activeItem } = useWrenchNav();

  if(activeItem?.type !== 'ENTITY_EDITOR') {
    return { enabled: false, onSave: () => {} }
  }
  const entity = composer.session.getEntity(activeItem.id);
  if (!entity) {
    return { enabled: false, onSave: () => {} }
  }

  const unsaved = Object.values(composer.session.pages)
    .filter(p => !p.saved)
    .find(p => p.origin.id === entity.id);

  const enabled = !!unsaved;

  function onSave() {
    if(!enabled || !entity) {
      return;
    }
    const unsavedArticlePages: WrenchComposerApi.PageUpdate = unsaved;
    composer.service.update(entity.id, unsavedArticlePages.value).then(async success => {
      await composer.actions.handleLoadSite(success)
      composer.actions.handlePageUpdateRemove([entity.id]);
      enqueueSnackbar(<FormattedMessage id="activities.assets.saveSuccess" values={{ name: entity.ast?.name }} />);
    }).catch((_error) => {

    });
  }
  return { enabled, onSave }
}


export const WrenchStickySave: React.FC<{}> = ({  }) => {
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

/*
'& .EveliShell-unsaved': {
  color: ownerState.unsaved ? theme.palette.common.black : theme.palette.text.secondary,
  backgroundColor: alpha(theme.palette.warning.main, 0.8),
  padding: theme.spacing(1)
}*/