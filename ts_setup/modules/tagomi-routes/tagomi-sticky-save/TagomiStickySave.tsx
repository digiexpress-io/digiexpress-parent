import React from 'react';
import { Button, lighten, Typography, useTheme } from '@mui/material';
import { SaveOutlined as SaveOutlinedIcon } from '@mui/icons-material';

import { FormattedMessage, useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';


import { TagomiApi, TagomiComposerApi } from '@dxs-ts/tagomi-api';
import { useTagomiNav } from '../tagomi-nav';


function useSave() {
  const { enqueueSnackbar } = useSnackbar();
  const composer = TagomiComposerApi.useComposer();
  const { activeItem } = useTagomiNav();

  if (activeItem?.type !== 'SERVICE_TEMPLATES') {
    return { enabled: false, onSave: () => { } }
  }

  const service = activeItem?.type === "SERVICE_TEMPLATES" ? composer.site.services[activeItem.service] : undefined;
  const unsavedPages = Object.values(composer.session.templates).filter(p => !p.saved);
  const unsavedArticlePages: TagomiComposerApi.TemplateUpdate[] = (service ? unsavedPages.filter(p => !p.saved).filter(p => p.origin.serviceId === service.id) : []);

  const enabled = unsavedArticlePages.length > 0;
  const message = <FormattedMessage id="snack.template.savedMessage" />


  const onSave = (_event: React.SyntheticEvent) => {
    if (!enabled || !service) {
      return;
    }

    if (service) {
      if (unsavedArticlePages.length === 0) {
        return;
      }
      const update: TagomiApi.TemplateMutator[] = unsavedArticlePages
        .map(p => ({ templateId: p.origin.id, locale: p.origin.localeId, content: p.value }));

      composer.backend.updateTemplate(update).then(success => {
        return composer.actions.handleLoadSite().then(() => {
          enqueueSnackbar(message, { variant: 'success' });
          composer.actions.handleTemplateUpdateRemove(success.map(p => p.id));
        })
      });
    }
  }
  return { enabled, onSave }
};



export const TagomiStickySave: React.FC = () => {
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