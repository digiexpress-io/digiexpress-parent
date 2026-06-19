import React from 'react';

import { Fs, useFsDirentBody } from '@dxs-ts/fs-api';
import MonacoReact, { useMonaco, OnChange, OnMount, BeforeMount } from '@monaco-editor/react';
import * as monaco_editor from 'monaco-editor';

import { CompletionBuilder, EXTERNAL_DIALOG, CompletionDialogProps } from './autocomplete';
import { SelectOrCreateAsset } from './SelectOrCreateAsset';



function _getErrorLine(model: monaco_editor.editor.ITextModel, reference: Fs.ModelError): number {
  
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


export const MonacoIntegration: React.FC<{
  id: string;
  src: string;
  messages: Fs.ModelError[];
  onChange: (newText: string) => void;
  flow: Fs.WrenchAstBody<Fs.FlowAst>
}> = (props) => {

  const { body: site } = useFsDirentBody();
  const { messages, onChange, flow } = props;
  const ast = flow.ast;
  const monaco: typeof monaco_editor | null = useMonaco();
  const astRef = React.useRef<Fs.FlowAst | undefined>(ast);
  // tracks the registered provider so it can be disposed
  const flowYamlCompletionRef = React.useRef<monaco_editor.IDisposable | null>(null);
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

  const beforeMount: BeforeMount = React.useCallback((monaco) => {
    monaco.editor.addCommand({
      id: EXTERNAL_DIALOG,
      run: function(...args) {
        setGuided(args[1].autocomplete);
      },
    });
  }, []);

  const handleEditorMount: OnMount = React.useCallback((editor, monaco) => {
    const model = editor.getModel();
    flowYamlCompletionRef.current?.dispose();
    flowYamlCompletionRef.current = monaco.languages.registerCompletionItemProvider('yaml', {
      provideCompletionItems(m, position) {
        // Monaco registers completion providers per language, not per editor, so this callback fires for every YAML model in the workspace
        // so only serve suggestions for this editor's own model
        if (!astRef.current || !model || m !== model) {
          return { suggestions: [] };
        }
        const suggestions = new CompletionBuilder()
          .withFlow(astRef.current)
          .withSite(site)
          .withModel(m)
          .withPosition(position)
          .build();
        return { suggestions };
      },
    });

    // dispose the provider when the editor is disposed
    editor.onDidDispose(() => {
      flowYamlCompletionRef.current?.dispose();
      flowYamlCompletionRef.current = null;
    });
  }, []);

  return (
  <>
    {guided && monaco ? <SelectOrCreateAsset onClose={() => setGuided(undefined)} flow={props.flow} guided={guided} cm={monaco}/> : undefined}
    <MonacoReact 
      beforeMount={beforeMount}
      onMount={handleEditorMount}
      onChange={handleChange}
      value={props.src} 
      options={{
        padding: {
          top: 0,
          bottom: 100, // Adjust this number (in pixels) for how much extra space you want
        },
        wordBasedSuggestions: 'off',
        minimap: {
          enabled: false
        }
      }}
      defaultLanguage='yaml'/>
  </>);
}