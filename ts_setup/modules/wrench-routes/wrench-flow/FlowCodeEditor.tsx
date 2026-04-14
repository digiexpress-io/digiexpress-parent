import React from 'react';


import MonacoReact, { useMonaco, OnChange, BeforeMount } from '@monaco-editor/react';
import * as monaco_editor from 'monaco-editor';

import { HdesApi } from '@dxs-ts/wrench-api';
import { WrenchComposerApi } from '@dxs-ts/wrench-api';

import { CompletionBuilder, EXTERNAL_DIALOG, CompletionDialogProps } from './autocomplete';
import { SelectOrCreateAsset } from './SelectOrCreateAsset';



function _getErrorLine(model: monaco_editor.editor.ITextModel, reference: HdesApi.FlowAstCommandMessage): number {
  
  if(reference.line !== undefined && reference.line !== null) {
    return reference.line;
  }

  const text = reference.msg;
  const errorWithoutAst = text.indexOf("Task: '");

  if(errorWithoutAst === -1) {
    return 1;
  }

  const taskRefDirty = text.substring(7);
  const taskRef = taskRefDirty.substring(0, taskRefDirty.indexOf("'"));
  const length = model.getLineCount();
  
  let taskLine: number = -1;
  for(let lineNumber = 1; lineNumber < length; lineNumber++) {
    
    const lineContent = model.getLineContent(lineNumber)
      .replace("'", "")
      .replace('"', "")
    const isTaskFound = lineContent.indexOf("id: " + taskRef) > -1;
    
    if(isTaskFound) {
      taskLine = lineNumber;
      break;
    }
  }

  const paramRef = text.indexOf('@from:')
  if(paramRef > -1 && taskLine > -1) {

    const paramName = text.substring(paramRef + 7).trim();
    for(let lineNumber = taskLine; lineNumber < length; lineNumber++) {
      
      const lineContent = model.getLineContent(lineNumber)
      const isParamFound = lineContent.indexOf(paramName) > -1;
      
      if(isParamFound) {
        taskLine = lineNumber;
        break;
      }
    }
  }

  if(taskLine > -1) {
    return taskLine;
  }

  return 1;
}


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
      .map(reference => {
        
        const text = reference.msg;
        const lineNumber = _getErrorLine(model, reference);
        console.error(reference);

        if (lineNumber < 1 || lineNumber > lineCount) {
          return null;
        }
  
        const content = model.getLineContent(lineNumber);
        return {
          message: text,
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