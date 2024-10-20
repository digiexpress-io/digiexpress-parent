import { BurgerApi } from '../../BurgerApi';

enum ReducerActionType {
  setDrawer = "setDrawer",
}

interface ReducerAction {
  type: ReducerActionType;
  setDrawer?: boolean;
}

class DrawerReducerDispatch implements BurgerApi.DrawerActions {

  private _sessionDispatch: React.Dispatch<ReducerAction>;
  constructor(session: React.Dispatch<ReducerAction>) {
    console.log("burger: init drawer dispatch");
    this._sessionDispatch = session;
  }
  handleDrawerOpen(drawerOpen: boolean) {
    this._sessionDispatch({ type: ReducerActionType.setDrawer, setDrawer: drawerOpen });
  }
}

const DrawerReducer = (state: BurgerApi.DrawerSession, action: ReducerAction): BurgerApi.DrawerSession => {
  switch (action.type) {
    case ReducerActionType.setDrawer: {
      if (action.setDrawer === undefined) {
        console.error("Action data error", action);
        return state;
      }
      return state.withDrawer(action.setDrawer);
    }
  }
}

export { DrawerReducer, DrawerReducerDispatch };
