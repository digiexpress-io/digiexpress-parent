import * as API from './SecondaryAPI';

enum ReducerActionType {
  setSecondary = "setSecondary"
}

interface ReducerAction {
  type: ReducerActionType;
  setSecondary?: string;
}

class SecondaryReducerDispatch implements API.SecondaryActions {

  private _sessionDispatch: React.Dispatch<ReducerAction>;
  constructor(session: React.Dispatch<ReducerAction>) {
    console.log("portal: init secondary dispatch");
    this._sessionDispatch = session;
  }
  handleSecondary(newItemId?: string) {
    this._sessionDispatch({ type: ReducerActionType.setSecondary, setSecondary: newItemId });
  }
}

const SecondaryReducer = (state: API.SecondarySession, action: ReducerAction): API.SecondarySession => {
  switch (action.type) {
    case ReducerActionType.setSecondary: {
      return state.withSecondary(action.setSecondary);
    }
  }
}

export { SecondaryReducer, SecondaryReducerDispatch };
