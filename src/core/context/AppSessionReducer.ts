import * as API from './AppAPI';

enum ReducerActionType {
  setActive = "setDrawer",
}

interface ReducerAction {
  type: ReducerActionType;
  setActive?: API.AppId;
}

class AppReducerDispatch implements API.AppActions {

  private _sessionDispatch: React.Dispatch<ReducerAction>;
  private _children: API.App<any>[];
  
  constructor(session: React.Dispatch<ReducerAction>, children: API.App<any>[]) {
    console.log("init drawer dispatch");
    this._sessionDispatch = session;
    this._children = children;
  }
  handleActive(active: API.AppId) {
    this._sessionDispatch({ type: ReducerActionType.setActive, setActive: active });
  }
}

const AppReducer = (state: API.AppSession, action: ReducerAction): API.AppSession => {
  switch (action.type) {
    case ReducerActionType.setActive: {
      if (action.setActive === undefined) {
        console.error("Action data error", action);
        return state;
      }
      return state.withActive(action.setActive);
    }
  }
}

export { AppReducer, AppReducerDispatch };
