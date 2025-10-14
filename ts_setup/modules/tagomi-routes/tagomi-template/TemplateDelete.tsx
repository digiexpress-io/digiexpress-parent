import React from 'react';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material'
import { useSnackbar } from 'notistack';
import { FormattedMessage } from 'react-intl';

import * as Burger from '@dxs-ts/eveli-primitives';
import { CancelButton } from '@dxs-ts/eveli-primitives';
import { TagomiApi, TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';
import { useTagomiNav } from '../tagomi-nav';



export const TemplateDelete: React.FC<{ onClose: () => void, serviceId: TagomiApi.ServiceId }> = ({ onClose, serviceId }) => {
  const { enqueueSnackbar } = useSnackbar();
  const { site, backend, actions } = Composer.useComposer();
  const [pageId, setPageId] = React.useState('');
  const { onTabClose, findTab } = useTagomiNav();
  const service = site.services[serviceId];
  const templates = Object.values(site.templates).filter((template) => template.serviceId === serviceId);
  const message = <FormattedMessage id="snack.page.deletedMessage" />




  const handleDelete = () => {
    const templateTab = findTab('SERVICE_TEMPLATES', serviceId);

    const entity: TagomiApi.TemplateId = pageId;
    backend.deleteTemplate(entity).then(async _success => {
      if (templateTab) {
        onTabClose(templateTab);
      }
      await actions.handleLoadSite();
      enqueueSnackbar(message, { variant: 'warning' });
      onClose();
    })
  }


  function getLocaleCode(template: TagomiApi.Template) {
    return (site.locales[template.localeId]?.localeCode ?? template.localeId)
  }

  function getLocaleLabel(template: TagomiApi.Template) {
    return (service.labels.find((label) => label.locale === template.localeId)?.labelValue ?? '');
  }



  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='tagomi.template.delete.dialog.title' />
        <FormattedMessage id='eveli.textSeparatorColon' />
        {service.serviceName}
      </DialogTitle>
      <DialogContent>
        <FormattedMessage id='tagomi.template.delete.desc' />
        <Burger.Select
          selected={pageId}
          onChange={setPageId}
          label='tagomi.template.selectTemplate'
          items={templates.map(template => ({
            id: template.id,
            value: `${getLocaleCode(template)} - ${getLocaleLabel(template)}`
          }))}
        />
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleDelete} disabled={!pageId}>
          <FormattedMessage id='tagomi.button.delete.template' />
        </Button>
      </DialogActions>
    </Dialog>
  );
}



