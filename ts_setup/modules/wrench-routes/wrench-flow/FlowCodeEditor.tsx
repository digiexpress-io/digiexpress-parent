import React from 'react';


import MonacoReact, { useMonaco, OnChange, BeforeMount } from '@monaco-editor/react';
import * as monaco_editor from 'monaco-editor';

import { HdesApi } from '@dxs-ts/wrench-api';
import { WrenchComposerApi } from '@dxs-ts/wrench-api';

import { CompletionBuilder, EXTERNAL_DIALOG, CompletionDialogProps } from './autocomplete';
import { SelectOrCreateAsset } from './SelectOrCreateAsset';


export const FlowCodeEditor: React.FC<{
  id: string;
  src: string;
  messages: HdesApi.FlowAstCommandMessage[];
  onChange: (newText: string) => void;
  ast: HdesApi.AstFlow | undefined;
  flow: HdesApi.Entity<HdesApi.AstFlow>
}> = (props) => {

  const { site } = WrenchComposerApi.useComposer();
  const { messages, onChange, ast } = props;
  const monaco: typeof monaco_editor | null = useMonaco();
  const astRef = React.useRef<HdesApi.AstFlow | undefined>(ast);
  const [guided, setGuided] = React.useState<CompletionDialogProps>();

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
  

  const handleChange: OnChange = (newValue) => {
    onChange(newValue ?? '');
  }

  React.useEffect(() => {
    astRef.current = ast;
  }, [ast]);

  const beforeMount: BeforeMount = React.useCallback((editor) => {

    editor.editor.addCommand({
      id: EXTERNAL_DIALOG, 
      run: function(...args) {
        setGuided(args[1].autocomplete)
      }
    });

    editor.languages.registerCompletionItemProvider('yaml', {
      provideCompletionItems: function (model, position, context) {

        let suggestions = astRef.current ? new CompletionBuilder()
          .withFlow(astRef.current)
          .withSite(site)
          .withModel(model)
          .withPosition(position)
          .build() : [];

        return { suggestions };
      }
    });

  }, []); 

  return (
  <>
    {guided && monaco ? <SelectOrCreateAsset onClose={() => setGuided(undefined)} flow={props.flow} guided={guided} cm={monaco}/> : undefined}
    <MonacoReact 
      beforeMount={beforeMount}
      onChange={handleChange}
      value={props.src} 
      options={{
        wordBasedSuggestions: 'off',
        minimap: {
          enabled: false
        }
      }}
      defaultLanguage='yaml'/>
  </>);
}