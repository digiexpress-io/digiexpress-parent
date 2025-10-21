import React from 'react';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material'
import { useIntl } from 'react-intl';


import * as Burger from '@dxs-ts/eveli-primitives';
import { CancelButton } from '@dxs-ts/eveli-primitives';
import { TagomiApi, TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';
import { DebugForm } from './DebugForm';
import { DebugPdfViewer } from './DebugPdfViewer';


export const DebugLocale: React.FC<{ 
  serviceId: string;
  onClose: () => void;
}> = ({ serviceId, onClose }) => {
  
  const intl = useIntl();
  const { site, backend } = Composer.useComposer();

  const [locale, setLocale] = React.useState('');
  const [input, setInput] = React.useState<object>({});
  const [base64, setBase64] = React.useState<string>(); 

  const service = site.services[serviceId];
  const templates = Object.values(site.templates).filter((template) => template.serviceId === serviceId);
  const noTemplates = templates.length === 0;

  function getLocaleCode(template: TagomiApi.Template) {
    return (site.locales[template.localeId]?.localeCode ?? template.localeId)
  }

  function getLocaleLabel(template: TagomiApi.Template) {
    return (service.labels.find((label) => label.locale === template.localeId)?.labelValue ?? '');
  }

  function handleInput(newInput: object) {
    setInput(newInput);
  }

  async function handleCompile() {
    const pdf = await backend.compileTemplate(serviceId, locale, input);
    if(pdf.status === 'OK') {
      setBase64(pdf.value?.bodyBase64);
    } else {
      setBase64(undefined);
    }
  }

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'tagomi.template.debug.dialog.title' })}
        {" "}
        {service.serviceName}
      </DialogTitle>
      <DialogContent>
        {intl.formatMessage({ id: 'tagomi.template.debug.dialog.desc' })}
        <Burger.Select
          selected={locale}
          disabled={noTemplates}
          onChange={setLocale}
          label='tagomi.template.debug.dialog.selectTemplate'
          items={templates.map(template => ({
            id: template.localeId,
            value: `${getLocaleCode(template)} - ${getLocaleLabel(template)}`
          }))}
          helperText={noTemplates ? intl.formatMessage({ id: 'tagomi.template.debug.dialog.noTemplates.helperText' }) : undefined}
        />
        <DebugForm selected={service.orchestratorName} onChange={handleInput} />
        {base64 ? <DebugPdfViewer base64={base64} /> : <>no pdf</> }

      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleCompile} disabled={!locale}>
          {intl.formatMessage({ id: 'tagomi.button.debug.dialog.template' })}
        </Button>
      </DialogActions>
    </Dialog>
  );
}