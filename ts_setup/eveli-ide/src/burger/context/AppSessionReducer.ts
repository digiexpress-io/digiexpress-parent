import { BurgerApi } from '../BurgerApi';

enum ReducerActionType {
  setActive = "setDrawer",
}

interface ReducerAction {
  type: ReducerActionType;
  setActive?: BurgerApi.AppId;
}

class AppReducerDispatch implements BurgerApi.AppActions {

  private _sessionDispatch: React.Dispatch<ReducerAction>;
  private _children: BurgerApi.App<any>[];
  
  constructor(session: React.Dispatch<ReducerAction>, children: BurgerApi.App<any>[]) {
    console.log("burger: init app dispatch");
    this._sessionDispatch = session;
    this._children = children;
  }
  handleActive(active: BurgerApi.AppId) {
    this._sessionDispatch({ type: ReducerActionType.setActive, setActive: active });
  }
}

const AppReducer = (state: BurgerApi.AppSession, action: ReducerAction): BurgerApi.AppSession => {
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
