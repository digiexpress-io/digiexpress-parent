import React from 'react';
import { FormattedMessage, useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';

import * as Burger from '@dxs-ts/eveli-primitives';
import { CancelButton } from '@dxs-ts/eveli-primitives';

import { TagomiApi, TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';


export const TemplateLocaleEdit: React.FC<{ onClose: () => void, serviceId: TagomiApi.ServiceId }> = (props) => {
  const { enqueueSnackbar } = useSnackbar();
  const intl = useIntl();
  const { backend, actions, site } = Composer.useComposer();
  const [templateId, setTemplateId] = React.useState('');
  const [newLocale, setNewLocale] = React.useState('');

  const valid = templateId && newLocale;
  const message = <FormattedMessage id="snack.template.savedMessage" />

  const service = site.services[props.serviceId];
  const serviceTemplates = Object.values(site.templates).filter(t => t.serviceId === service.id);
  const usedLocaleIds = serviceTemplates.map(t => t.localeId);
  const availableLocales = service.labels
    .filter(label => !usedLocaleIds.includes(label.locale))
    .map(label => ({
      id: label.locale,
      value: site.locales[label.locale]?.localeCode ?? label.locale
    }));

  const templateItems = serviceTemplates.map(template => ({
    id: template.id,
    value: `${site.locales[template.localeId]?.localeCode ?? template.localeId} - ${service.labels.find(l => l.locale === template.localeId)?.labelValue ?? ''}`
  }));

  const noTemplates = serviceTemplates.length === 0;
  const noAvailableLocales = availableLocales.length === 0;

  const handleUpdate = () => {
    const entity: TagomiApi.TemplateMutator = { locale: newLocale, templateId, content: '#content', resourceIds: [] };
    backend.updateTemplate([entity]).then(_success => {
      enqueueSnackbar(message, { variant: 'success' });
      props.onClose();
      actions.handleLoadSite();
    })
  }

  return (
    <Dialog open={true} onClose={props.onClose}>
      <DialogTitle><FormattedMessage id='tagomi.template.change.dialog.title' /></DialogTitle>
      <DialogContent>
        <FormattedMessage id='tagomi.template.change.dialog.desc' />

        <Burger.Select
          selected={templateId}
          onChange={setTemplateId}
          label={intl.formatMessage({ id: 'tagomi.template.change.dialog.selectOrigin' })}
          items={templateItems}
          disabled={noTemplates}
          helperText={noTemplates ? intl.formatMessage({ id: 'tagomi.template.change.dialog.noTemplates' }) : undefined}
        />

        <Burger.Select
          selected={newLocale}
          onChange={setNewLocale}
          label={intl.formatMessage({ id: 'tagomi.template.change.dialog.selectTarget' })}
          items={availableLocales}
          disabled={noAvailableLocales || noTemplates}
          helperText={noAvailableLocales ? intl.formatMessage({ id: 'tagomi.template.change.dialog.noLocales' }) : undefined}
        />
      </DialogContent>

      <DialogActions>
        <CancelButton onClick={props.onClose} />
        <Button onClick={handleUpdate} disabled={!valid}>
          <FormattedMessage id='button.update' />
        </Button>
      </DialogActions>
    </Dialog>
  );
}

