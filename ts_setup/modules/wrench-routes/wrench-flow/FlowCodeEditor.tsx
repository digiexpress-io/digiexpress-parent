import React from 'react';


import MonacoReact, { useMonaco, OnChange, BeforeMount } from '@monaco-editor/react';
import * as monaco_editor from 'monaco-editor';

import { HdesApi } from '@dxs-ts/wrench-api';
import { WrenchComposerApi } from '@dxs-ts/wrench-api';

import { AutocompleteVisitor, EXTERNAL_DIALOG, FlowAstAutocomplete } from './autocomplete';
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
  const [guided, setGuided] = React.useState<FlowAstAutocomplete>();

  React.useEffect(() => {
    if(!monaco) {
      return;
    }
    const [model] = monaco.editor.getModels();
    if(!model) {
      return;
    }


    monaco.editor.setModelMarkers(model, "owner", 
      messages.map(msg => {
        const content = model.getLineContent(msg.line+1);
        return {
          message: msg.value,
          severity: msg.type === 'WARNING' ? monaco.MarkerSeverity.Warning : monaco.MarkerSeverity.Error,
          startLineNumber: msg.line+1,
          endLineNumber: msg.line+1,

          startColumn: 1,
          endColumn: content.length+1,
        }
    }))

  }, [messages, monaco]);

  React.useEffect(() => {
    if(!monaco) {
      return;
    }
  }, []);


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
        let suggestions = astRef.current ? new AutocompleteVisitor(astRef.current, site, model, position).visit() : [];
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