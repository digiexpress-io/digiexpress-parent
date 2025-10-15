
import React from 'react';
import MonacoReact, { useMonaco, OnChange, BeforeMount } from '@monaco-editor/react';
import * as monaco_editor from 'monaco-editor';


import { TagomiComposerApi } from '@dxs-ts/tagomi-api';
import { Box, Typography } from '@mui/material';
import { useIntl } from 'react-intl';


interface Message {
  line: number;
  value: string;
  type: 'WARNING' | 'ERROR'
}

export const TemplateEditor: React.FC<{ serviceId: string, templateId: string }> = ({ serviceId, templateId }) => {
  const intl = useIntl();
  const messages: Message[] = [];
  const monaco: typeof monaco_editor | null = useMonaco();
  const { site } = TagomiComposerApi.useComposer();
  const composer = TagomiComposerApi.useComposer();

  const template = site.templates[templateId];
  const service = site.services[serviceId];
  const localeLabel = service.labels.find(l => l.locale === template.localeId)?.labelValue ?? '';

  const [src, setSrc] = React.useState(template.content);

  React.useEffect(() => {
    if (!monaco) {
      return;
    }
  }, []);

  const beforeMount: BeforeMount = React.useCallback((editor) => {

  }, []);

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
  };

  return (
    <Box height="calc(100vh - 64px)" p={2}>
      <Typography variant='h1'>{intl.formatMessage({ id: 'tagomi.template.editor.title' })}</Typography>
      <Typography fontWeight={500}>{intl.formatMessage({ id: 'tagomi.template.editor.serviceName' })}{" "}{service.serviceName}</Typography>
      <Typography fontWeight={500}>{intl.formatMessage({ id: 'tagomi.template.editor.localeLabel' })}{" "}{localeLabel}</Typography>
      <Box mb={3} />
      <MonacoReact
        beforeMount={beforeMount}
        onChange={handleChange}
        value={src}
        options={{
          wordBasedSuggestions: 'off',
          minimap: {
            enabled: false
          }
        }}
        defaultLanguage='yaml' />
    </Box>);
}