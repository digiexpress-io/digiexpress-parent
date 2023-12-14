import React from 'react';
import API from '../api';

declare namespace Editor {
  interface Actions {
    addCommands(newCommands: API.ViewCommand[]): void;
    setEditor(ref: React.RefObject<HTMLTextAreaElement>): void;
    setConfig(config: API.ViewProps): void;
    setEvents(lintHint: API.ViewEvents): void;
  }
  interface Session {
    config?: API.ViewProps;
    view?: API.View;
    commands: API.ViewCommand[];
  }
  interface SessionMutator extends Session {
    withConfig(view: API.ViewProps): SessionMutator;
    withView(view: API.View): SessionMutator;
    withCommands(commands: API.ViewCommand[]): SessionMutator;
    withEvents(lintHint: API.ViewEvents): SessionMutator;
  }
  interface ContextType {
    actions: Actions,
    session: Session
  }
}


class EditorSessionData implements Editor.SessionMutator {
  private _view?: API.View;
  private _config?: API.ViewProps;
  private _commands: API.ViewCommand[];

  constructor(props: {view?: API.View, config?: API.ViewProps, commands: API.ViewCommand[]}) {
    this._view = props.view;
    this._config = props.config;
    this._commands = props.commands;
  }
  get view() {
    return this._view;
  }
  get commands() {
    return this._commands;
  }
  get config() {
    return this._config;
  }
  withConfig(config: API.ViewProps): Editor.SessionMutator {
    return new EditorSessionData({view: this._view, config, commands: this._commands});
  }
  withView(view: API.View): Editor.SessionMutator {
    return new EditorSessionData({view, commands: this._commands, config: this._config});
  }
  withCommands(commands: API.ViewCommand[]): Editor.SessionMutator {
    return new EditorSessionData({view: this._view, commands: API.optimize(commands), config: this._config});
  }
  withEvents(events: API.ViewEvents): Editor.SessionMutator {
    if(!this._view) {
      throw new Error("Editor not created!");
    }
    return new EditorSessionData({view: this._view.withEvents(events), config: this._config, commands: this._commands}); 
  }
}


namespace Editor {
  export const Context = React.createContext<ContextType>({
    actions: {} as Actions,
    session: {} as Session,
  });

  export const sessionData = new EditorSessionData({ commands: []});

  export const useContext = () => {
    const result: ContextType = React.useContext(Context);
    return result;
  }
}

export default Editor;