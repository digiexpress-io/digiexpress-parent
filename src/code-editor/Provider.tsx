import React from 'react';

import Integration from './Integration';
import API from './api';
import { ReducerDispatch, Reducer } from './context/Reducer';
import Editor from './context/Context';

interface ProviderProps {
  children: string;
  mode: "ft" | "fl";
  theme: 'light' | 'dark';
  onCommands: (commands: API.ViewCommand[]) => void;
  lint?: () => API.LintMessage[];
  hint?: (pos: CodeMirror.Position, content: string) => CodeMirror.Hints;
};

const Provider: React.FC<ProviderProps> = ({ children, mode, theme, onCommands, lint, hint }) => {

  const [session, dispatch] = React.useReducer(Reducer, Editor.sessionData);
  const actions = React.useMemo(() => new ReducerDispatch(dispatch, onCommands), [dispatch, onCommands]);

  React.useLayoutEffect(() => {
    if (session.config) {
      return;
    }
    actions.setConfig({
      src: children,
      theme: theme === 'light' ? 'eclipse' : 'monokai',
      mode: mode === "ft" ? "groovy" : "yaml",
    });
  }, [actions, children, mode, theme, session.config]);



  React.useLayoutEffect(() => {
    if(!session.view) {
      return;
    }
    actions.setEvents({
      onChanges: (newCommands, _value) => actions.addCommands(newCommands),
      lint: lint,
      hint: hint,
    })
  }, [lint, hint, onCommands, actions, session.view]);


  return (
    <Editor.Context.Provider value={{ session, actions }}>
      <Integration />
    </Editor.Context.Provider>

  );
}

export type { ProviderProps };
export { Provider };
