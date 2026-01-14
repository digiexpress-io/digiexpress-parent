
import React from 'react';
import MonacoReact, { useMonaco, OnChange, BeforeMount } from '@monaco-editor/react';
import * as monaco_editor from 'monaco-editor';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import { TagomiComposerApi } from '@dxs-ts/tagomi-api';
import { DebugPdfViewer, DebugForm, useGetFlowInput } from '../tagomi-debug';


interface Message {
  line: number;
  value: string;
  type: 'WARNING' | 'ERROR'
}

export const TemplateEditor: React.FC<{ serviceId: string, templateId: string }> = ({ serviceId, templateId }) => {
  const intl = useIntl();
  const messages: Message[] = [];
  const monaco: typeof monaco_editor | null = useMonaco();
  const composer = TagomiComposerApi.useComposer();
  const [inputOpen, setInputOpen] = React.useState<boolean>(false);
  const [base64, setBase64] = React.useState<string>();

  const template = composer.site.templates[templateId];
  const service = composer.site.services[serviceId];
  const localeLabel = service.labels.find(l => l.locale === template.localeId)?.labelValue ?? '';
  const [src, setSrc] = React.useState(template.content);
  const { input, setInput } = useGetFlowInput(service.orchestratorName);

  async function handleCompile() {
    const pdf = await composer.backend.compileTemplate(serviceId, template.localeId, input);
    if (pdf.status === 'OK') {
      setBase64(pdf.value?.bodyBase64);
    } else {
      setBase64(undefined);
    }
  }

  function handleInputOpen() {
    setInputOpen(true);
  }
  function handleInputClose() {
    setInputOpen(false);
  }
  React.useEffect(() => {
    if (!monaco) {
      return;
    }
    const [model] = monaco.editor.getModels();
    if (!model) {
      return;
    }
    monaco.editor.setModelMarkers(model, "owner",
      messages.map(msg => {
        const content = model.getLineContent(msg.line + 1);
        return {
          message: msg.value,
          severity: msg.type === 'WARNING' ? monaco.MarkerSeverity.Warning : monaco.MarkerSeverity.Error,
          startLineNumber: msg.line + 1,
          endLineNumber: msg.line + 1,

          startColumn: 1,
          endColumn: content.length + 1,
        }
      }))

  }, [messages, monaco]);

  const handleChange: OnChange = (newValue) => {
    const content = newValue ?? '';
    setSrc(content);

    composer.actions.handleTemplateUpdate(templateId, content);
  }

  return (
    <Box height="calc(100vh - 64px)">
      <Box p={2}>
        <Typography variant='h1'>{intl.formatMessage({ id: 'tagomi.template.editor.title' })}</Typography>
        <Typography fontWeight={500}>{intl.formatMessage({ id: 'tagomi.template.editor.serviceName' })}{" "}{service.serviceName}</Typography>
        <Typography fontWeight={500}>{intl.formatMessage({ id: 'tagomi.template.editor.localeLabel' })}{" "}{localeLabel}</Typography>

        <Box display='flex' justifyContent='center' gap={1} m={2} width='50%'>
          <Button onClick={handleInputOpen}>
            {intl.formatMessage({ id: 'tagomi.template.editor.fillInput' })}
          </Button>
          <Button onClick={handleCompile}>
            {intl.formatMessage({ id: 'tagomi.template.editor.compile' })}
          </Button>
        </Box>

        <Dialog open={inputOpen} onClose={handleInputClose}>
          <DialogTitle>{intl.formatMessage({ id: 'tagomi.template.debug.dialog.title' })}
            {" "}
            {service.serviceName}
          </DialogTitle>
          <DialogContent>
            {intl.formatMessage({ id: 'tagomi.template.debug.dialog.desc' })}
            <DebugForm selected={service.orchestratorName} onChange={setInput} />
          </DialogContent>
          <DialogActions>
            <Button onClick={handleInputClose}>
              {intl.formatMessage({ id: 'button.accept' })}
            </Button>
          </DialogActions>
        </Dialog>
      </Box>

      <Box height={'100%'} width={'100%'} display='flex'>
        <Box height={'100%'} width={'50%'}>
          <MonacoReact
            onChange={handleChange}
            value={src}
            defaultLanguage='yaml'
            options={{
              wordBasedSuggestions: 'off',
              minimap: { enabled: false }
            }}
          />
        </Box>
        <Box height={'100%'} width={'50%'}>
          <DebugPdfViewer base64={base64 ?? ''} />
        </Box>
      </Box>
    </Box>);
}

