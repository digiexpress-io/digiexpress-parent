import API from '../api';
import Editor from './Context';

enum ActionType {
  setCommands = "setCommands",
  setEditor = "setEditor",
  setConfig = "setConfig",
  setEvents = "setEvents"
}

interface Action {
  type: ActionType;
  
  setEvents?: API.ViewEvents;
  setConfig?: API.ViewProps;
  setCommands?: API.ViewCommand[];
  setEditor?: React.RefObject<HTMLTextAreaElement>;
}

const ActionBuilder = {
  setEditor: (setEditor: React.RefObject<HTMLTextAreaElement>): Action => ({ type: ActionType.setEditor, setEditor }),
  setCommands: (setCommands: API.ViewCommand[]): Action => ({ type: ActionType.setCommands, setCommands }),
  setConfig: (setConfig: API.ViewProps): Action => ({ type: ActionType.setConfig, setConfig }),
  setEvents: (setEvents: API.ViewEvents): Action => ({ type: ActionType.setEvents, setEvents }),
}

class ReducerDispatch implements Editor.Actions {
  private _dispatch: React.Dispatch<Action>;
  private _onCommands: (commands: API.ViewCommand[]) => void;

  constructor(dispatch: React.Dispatch<Action>, onCommands: (commands: API.ViewCommand[]) => void) {
    this._dispatch = dispatch;
    this._onCommands = onCommands;
  }
  setEditor(ref: React.RefObject<HTMLTextAreaElement>): void {
    this._dispatch(ActionBuilder.setEditor(ref))
  }
  setConfig(config: API.ViewProps): void {
    this._dispatch(ActionBuilder.setConfig(config))
  }
  setEvents(events: API.ViewEvents): void {
    this._dispatch(ActionBuilder.setEvents(events))
  }
  addCommands(newCommands: API.ViewCommand[]): void {
    if(newCommands.length > 0) {
      this._onCommands(newCommands);
    }
    this._dispatch(ActionBuilder.setCommands(newCommands))
  }
}

const Reducer = (state: Editor.SessionMutator, action: Action): Editor.SessionMutator => {
  switch (action.type) {
    case ActionType.setCommands: {
      if (action.setCommands) {
        return state.withCommands(action.setCommands);
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.setConfig: {
      if (action.setConfig) {
        return state.withConfig(action.setConfig);
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.setEvents: {
      if (action.setEvents) {
        return state.withEvents(action.setEvents);
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.setEditor: {
      if (action.setEditor && state.config) {
        const ref = action.setEditor;
        const view = API.create(ref, state.config);
        return state.withView(view);
      }
      return state;
    }
  }
}

export type { Action }
export { Reducer, ReducerDispatch, ActionType };
