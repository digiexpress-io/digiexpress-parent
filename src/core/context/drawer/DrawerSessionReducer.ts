import * as API from './DrawerAPI';

enum ReducerActionType {
  setDrawer = "setDrawer",
}

interface ReducerAction {
  type: ReducerActionType;
  setDrawer?: boolean;
}

class DrawerReducerDispatch implements API.DrawerActions {

  private _sessionDispatch: React.Dispatch<ReducerAction>;
  constructor(session: React.Dispatch<ReducerAction>) {
    console.log("portal: init drawer dispatch");
    this._sessionDispatch = session;
  }
  handleDrawerOpen(drawerOpen: boolean) {
    this._sessionDispatch({ type: ReducerActionType.setDrawer, setDrawer: drawerOpen });
  }
}

const DrawerReducer = (state: API.DrawerSession, action: ReducerAction): API.DrawerSession => {
  switch (action.type) {
    case ReducerActionType.setDrawer: {
      if (action.setDrawer === undefined) {
        console.error("Action data error", action);
        return state;
      }
      if(action.setDrawer === state.drawer) {
        return state;
      }
      
      return state.withDrawer(action.setDrawer);
    }
  }
}

export { DrawerReducer, DrawerReducerDispatch };
