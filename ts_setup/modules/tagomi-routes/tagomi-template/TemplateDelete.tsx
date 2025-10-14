import React from 'react';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material'
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';

import * as Burger from '@dxs-ts/eveli-primitives';
import { CancelButton } from '@dxs-ts/eveli-primitives';
import { TagomiApi, TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';
import { useTagomiNav } from '../tagomi-nav';



export const TemplateDelete: React.FC<{ onClose: () => void, serviceId: TagomiApi.ServiceId }> = ({ onClose, serviceId }) => {
  const { enqueueSnackbar } = useSnackbar();
  const intl = useIntl();
  const { site, backend, actions } = Composer.useComposer();
  const [pageId, setPageId] = React.useState('');
  const { onTabClose, findTab } = useTagomiNav();
  const service = site.services[serviceId];
  const templates = Object.values(site.templates).filter((template) => template.serviceId === serviceId);
  const message = intl.formatMessage({ id: "snack.template.deletedMessage" });
  const noTemplates = templates.length === 0;


  function getLocaleCode(template: TagomiApi.Template) {
    return (site.locales[template.localeId]?.localeCode ?? template.localeId)
  }

  function getLocaleLabel(template: TagomiApi.Template) {
    return (service.labels.find((label) => label.locale === template.localeId)?.labelValue ?? '');
  }


  const handleDelete = () => {
    const templateTab = findTab('SERVICE_TEMPLATES', serviceId);

    const entity: TagomiApi.TemplateId = pageId;
    backend.deleteTemplate(entity).then(async _success => {
      if (templateTab) {
        onTabClose(templateTab);
      }
      await actions.handleLoadSite();
      enqueueSnackbar(message, { variant: 'success' });
      onClose();
    })
  }


  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'tagomi.template.delete.dialog.title' })}
        {" "}
        {service.serviceName}
      </DialogTitle>
      <DialogContent>
        {intl.formatMessage({ id: 'tagomi.template.delete.dialog.desc' })}
        <Burger.Select
          selected={pageId}
          disabled={noTemplates}
          onChange={setPageId}
          label='tagomi.template.delete.dialog.selectTemplate'
          items={templates.map(template => ({
            id: template.id,
            value: `${getLocaleCode(template)} - ${getLocaleLabel(template)}`
          }))}
          helperText={noTemplates ? intl.formatMessage({ id: 'tagomi.template.delete.dialog.noTemplates.helperText' }) : undefined}
        />
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleDelete} disabled={!pageId}>
          {intl.formatMessage({ id: 'tagomi.button.delete.dialog.template' })}
        </Button>
      </DialogActions>
    </Dialog>
  );
}



