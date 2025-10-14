import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';

import * as Burger from '@dxs-ts/eveli-primitives';

import { CancelButton } from '@dxs-ts/eveli-primitives';
import { useTagomiNav } from '../tagomi-nav';
import { TagomiApi, TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';


export const NewTemplate: React.FC<{ onClose: () => void, serviceId?: TagomiApi.ServiceId }> = (props) => {
  const { enqueueSnackbar } = useSnackbar();
  const intl = useIntl();
  const { actions, site, backend } = Composer.useComposer();
  const [locale, setLocale] = React.useState('');
  const [serviceId, setServiceId] = React.useState<TagomiApi.ServiceId>(props.serviceId || '');
  const { onNav } = useTagomiNav();
  const message = intl.formatMessage({ id: 'snack.template.createdMessage' })

  const service = serviceId ? site.services[serviceId] : undefined;

  const handleCreate = () => {
    const entity: TagomiApi.CreateTemplate = { serviceId: service!.id, locale, content: '' };
    backend.createTemplate(entity).then(success => {
      enqueueSnackbar(message, { variant: 'success' });
      console.log(success)
      props.onClose();
      actions.handleLoadSite().then(() => {
        const service = site.services[serviceId];
        onNav({ article: service.id, type: "SERVICE_TEMPLATES", locale1: locale })
      });

    })
  }

  const definedLocales: TagomiApi.LocaleId[] = service ?
    Object.values(site.templates)
      .filter(template => template.serviceId === service.id)
      .map(template => template.localeId) : [];

  const availableLocaleLabels = service ? service.labels.filter((label) => !definedLocales.includes(label.locale)) : [];


  return (
    <Dialog open={true} onClose={props.onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'tagomi.template.create.dialog.title' })}</DialogTitle>
      <DialogContent>
        {intl.formatMessage({ id: 'tagomi.template.create.dialog.desc' })}

        <Burger.Select
          selected={serviceId}
          onChange={setServiceId}
          label='tagomi.template.create.dialog.service.name'
          items={Object.values(site.services).map((service) => ({
            id: service.id,
            value: service.serviceName
          }))}
        />

        <Burger.Select
          selected={locale}
          onChange={setLocale}
          disabled={!service || availableLocaleLabels.length === 0}
          label='tagomi.template.create.dialog.localeLabel.select'
          items={availableLocaleLabels.map((item) => ({
            id: item.locale,
            value: item.labelValue
          }))}
          helperText={service && availableLocaleLabels.length === 0 ? intl.formatMessage({ id: 'tagomi.template.create.dialog.noLocales' }) : undefined} 
        />

      </DialogContent>
      <DialogActions>
        <CancelButton onClick={props.onClose} />
        <Button onClick={handleCreate} disabled={!locale}>
          {intl.formatMessage({ id: 'button.create' })}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
