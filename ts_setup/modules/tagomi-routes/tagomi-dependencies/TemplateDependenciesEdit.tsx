import React from 'react';
import { FormattedMessage, useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions, Checkbox, ListItemText, FormHelperText, Typography } from '@mui/material';

import * as Burger from '@dxs-ts/eveli-primitives';
import { CancelButton } from '@dxs-ts/eveli-primitives';

import { TagomiApi, TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';


export const TemplateDependenciesEdit: React.FC<{ onClose: () => void, serviceId: TagomiApi.ServiceId }> = (props) => {
  const { enqueueSnackbar } = useSnackbar();
  const intl = useIntl();
  const { backend, actions, site } = Composer.useComposer();
  const [templateId, setTemplateId] = React.useState<string>('');
  const [selectedTemplates, setSelectedTemplates] = React.useState<string[]>([]);
  const [selectedResources, setSelectedResources] = React.useState<string[]>([]);

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

  const resourceItems = Object.values(site.resources).map(resource => ({
    id: resource.id,
    value: `${resource.resourceName} (${resource.contentType})`
  }));

  const noTemplates = serviceTemplates.length === 0;

  React.useEffect(() => {
    if (templateId) {
      const template = site.templates[templateId];  
      setSelectedTemplates(template.templateIds || []);      
      const linkedResources = Object.values(site.resources)
        .filter(r => r.templateIds.includes(templateId))
        .map(r => r.id);
      setSelectedResources(linkedResources);
    } else {
      setSelectedResources([]);
      setSelectedTemplates([]);
    }
  }, [templateId, site.resources, site.templates]);

  const handleUpdate = () => {
    const template = site.templates[templateId];
    if (!template) return;
    
    const entity: TagomiApi.TemplateMutator = { 
      templateId, 
      content: template.content,
      resourceIds: selectedResources,
      templateIds: selectedTemplates
    };
    backend.updateTemplate([entity]).then(_success => {
      enqueueSnackbar(message, { variant: 'success' });
      props.onClose();
      actions.handleLoadSite();
    })
  }


  return (
    <Dialog open={true} onClose={props.onClose}>
      <DialogTitle><FormattedMessage id='tagomi.template.dependencies.edit.dialog.title' />{": "}{service.serviceName}</DialogTitle>
      <DialogContent>
        <FormattedMessage id='tagomi.template.dependencies.edit.dialog.desc' />

        <Burger.Select
          selected={templateId}
          onChange={setTemplateId}
          label={intl.formatMessage({ id: 'tagomi.template.dependencies.edit.dialog.selectOrigin' })}
          items={templateItems}
          disabled={noTemplates}
          helperText={noTemplates ? intl.formatMessage({ id: 'tagomi.template.change.dialog.noTemplates' }) : undefined}
        />
        {!templateId && <FormHelperText error>{intl.formatMessage({ id: 'error.valueRequired' })}</FormHelperText>}

        {dependenciesTemplateItems.length > 0 && (
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
        )}

        {resourceItems.length > 0 && (
          <>
            <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>Resources (Images & Scripts)</Typography>
            <Burger.SelectMultiple
              multiline
              disabled={!templateId}
              selected={selectedResources}
              onChange={setSelectedResources}
              label={intl.formatMessage({ id: 'tagomi.template.dependencies.add.dialog.selectResources' }, { defaultMessage: 'Select resources' })}
              renderValue={(selected) => (
                selected as TagomiApi.ResourceId[])
                .map(id => resourceItems.find(r => r.id === id)?.value)
                .flatMap((item, index) => (item ? <div key={index}>{item}</div> : [])
                )}
              items={resourceItems.map((resource) => ({
                id: resource.id,
                value: (<>
                  <Checkbox checked={selectedResources.indexOf(resource.id) > -1} />
                  <ListItemText primary={resource.value} />
                </>
                )
              }))}
            />
          </>
        )}

        {!templateId && <FormHelperText>{intl.formatMessage({ id: 'tagomi.template.dependencies.add.dialog.helperText.noTemplateSelected' })}</FormHelperText>}
      </DialogContent>

      <DialogActions>
        <CancelButton onClick={props.onClose} />
        <Button onClick={handleUpdate} disabled={!templateId || (selectedTemplates.length === 0 && selectedResources.length === 0)}>
          <FormattedMessage id='button.update' />
        </Button>
      </DialogActions>
    </Dialog>
  );
}

