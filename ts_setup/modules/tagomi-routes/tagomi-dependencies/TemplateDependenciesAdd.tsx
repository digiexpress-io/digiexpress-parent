import React from 'react';
import { FormattedMessage, useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions, Checkbox, ListItemText, FormHelperText } from '@mui/material';

import * as Burger from '@dxs-ts/eveli-primitives';
import { CancelButton } from '@dxs-ts/eveli-primitives';

import { TagomiApi, TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';


export const TemplateDependenciesAdd: React.FC<{ onClose: () => void, serviceId: TagomiApi.ServiceId }> = (props) => {
  const { enqueueSnackbar } = useSnackbar();
  const intl = useIntl();
  const { backend, actions, site } = Composer.useComposer();
  const [templateId, setTemplateId] = React.useState<string>('');
  const [newLocale, setNewLocale] = React.useState('');
  const [selectedTemplates, setSelectedTemplates] = React.useState<string[]>([]);

  const valid = templateId && newLocale;
  const message = <FormattedMessage id="snack.template.savedMessage" />

  const service = site.services[props.serviceId];
  const serviceTemplates = Object.values(site.templates).filter(t => t.serviceId === service.id);

  const templateItems = serviceTemplates.map(template => ({
    id: template.id,
    value: `${site.locales[template.localeId]?.localeCode ?? template.localeId} - ${service.labels.find(l => l.locale === template.localeId)?.labelValue ?? ''}`
  }));

  const dependenciesTemplates = Object.values(site.templates).filter(t => t.serviceId !== service.id);
  const dependenciesTemplateItems = dependenciesTemplates.map(template => {
    const templateService = site.services[template.serviceId];
    const templateServiceName = site.services[template.serviceId].serviceName;
    return {
      id: template.id,
      value: `${templateServiceName}: ${site.locales[template.localeId]?.localeCode ?? template.localeId} - ${templateService?.labels.find(l => l.locale === template.localeId)?.labelValue ?? ''}`
    };
  });


  const noTemplates = serviceTemplates.length === 0;

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
      <DialogTitle><FormattedMessage id='tagomi.template.dependencies.add.dialog.title' />{": "}{service.serviceName}</DialogTitle>
      <DialogContent>
        <FormattedMessage id='tagomi.template.dependencies.add.dialog.desc' />

        <Burger.Select
          selected={templateId}
          onChange={setTemplateId}
          label={intl.formatMessage({ id: 'tagomi.template.dependencies.add.dialog.selectOrigin' })}
          items={templateItems}
          disabled={noTemplates}
          helperText={noTemplates ? intl.formatMessage({ id: 'tagomi.template.change.dialog.noTemplates' }) : undefined}
        />
        {!templateId && <FormHelperText error>{intl.formatMessage({ id: 'error.valueRequired' })}</FormHelperText>}


        <Burger.SelectMultiple
          multiline
          disabled={!templateId}
          selected={selectedTemplates}
          onChange={setSelectedTemplates}
          label={intl.formatMessage({ id: 'tagomi.template.dependencies.add.dialog.selectTargets' })}
          renderValue={(selected) => (
            selected as TagomiApi.TemplateId[])
            .map(id => dependenciesTemplateItems.find(template => template.id === id)?.value)
            .flatMap((item, index) => (item ? <div key={index}>{item}</div> : [])
            )}
          items={dependenciesTemplateItems.map((template) => ({
            id: template.id,
            value: (<>
              <Checkbox checked={selectedTemplates.indexOf(template.id) > -1} />
              <ListItemText primary={template.value} />
            </>
            )
          }))}
        />
        {!templateId && <FormHelperText>{intl.formatMessage({ id: 'tagomi.template.dependencies.add.dialog.helperText.noTemplateSelected' })}</FormHelperText>}
      </DialogContent>

      <DialogActions>
        <CancelButton onClick={props.onClose} />
        <Button onClick={handleUpdate} disabled={!templateId && selectedTemplates.length === 0}>
          <FormattedMessage id='button.update' />
        </Button>
      </DialogActions>
    </Dialog>
  );
}

