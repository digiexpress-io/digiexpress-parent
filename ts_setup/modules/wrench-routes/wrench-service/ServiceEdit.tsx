import React from 'react';
import { Box } from '@mui/material';

import MonacoReact, { useMonaco, OnChange, BeforeMount } from '@monaco-editor/react';
import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';
import * as monaco_editor from 'monaco-editor';


const ServiceEdit: React.FC<{service: HdesApi.Entity<HdesApi.AstService>}> = ({service}) => {
  const { actions, session } = Composer.useComposer();

  const handleChange = (value: string | undefined) => {
    actions.handlePageUpdate(service.id, value ?? '')
  }
  const update = session.pages[service.id];
  const src: string = (update && update.value ? update.value : service.ast?.value) as string;

  const monaco: typeof monaco_editor | null = useMonaco();

  const messages = service.errors;

  React.useEffect(function loadEditor() {
    if (!monaco) {
      return;
    }
  
    const model = monaco.editor.getModels()[0];
    if (!model) {
      return;
    }
    const lineCount = model.getLineCount();
  
    const markers = messages
      .map(msg => {
        const lineNumber = msg.line;
        if(!lineNumber) {
          return null;
        }
  
        if (lineNumber < 1 || lineNumber > lineCount) {
          return null;
        }
        console.error(msg)

        const content = model.getLineContent(lineNumber);
        return {
          message: msg.msg,
          severity: monaco.MarkerSeverity.Error,
          startLineNumber: lineNumber,
          endLineNumber: lineNumber,
          startColumn: 1,
          endColumn: content.length + 1,
        };
      })
      .filter((m): m is monaco_editor.editor.IMarkerData => m !== null);
  
    monaco.editor.setModelMarkers(model, "owner", markers);
  
  }, [messages, monaco]);
  

  return (<Box height="calc(100vh - 64px)">
    <MonacoReact 
      onChange={handleChange}
      value={src ? src : "#--failed-to-parse"}
      defaultLanguage='java'/>
  </Box>);
}

export { ServiceEdit };
