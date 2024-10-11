import { BurgerApi } from '../../BurgerApi';

enum ReducerActionType {
  setSecondary = "setSecondary"
}

interface ReducerAction {
  type: ReducerActionType;
  setSecondary?: string;
}

class SecondaryReducerDispatch implements BurgerApi.SecondaryActions {

  private _sessionDispatch: React.Dispatch<ReducerAction>;
  constructor(session: React.Dispatch<ReducerAction>) {
    console.log("burger: init secondary dispatch");
    this._sessionDispatch = session;
  }
  handleSecondary(newItemId?: string) {
    this._sessionDispatch({ type: ReducerActionType.setSecondary, setSecondary: newItemId });
  }
}

const SecondaryReducer = (state: BurgerApi.SecondarySession, action: ReducerAction): BurgerApi.SecondarySession => {
  switch (action.type) {
    case ReducerActionType.setSecondary: {
      if (!action.setSecondary) {
        console.error("Action data error", action);
        return state;
      }
      const result = state.withSecondary(action.setSecondary);
      return result;
    }
  }
}

export { SecondaryReducer, SecondaryReducerDispatch };
