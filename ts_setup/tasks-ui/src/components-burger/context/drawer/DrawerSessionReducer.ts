import * as API from './DrawerAPI';

import LoggerFactory from 'logger';
const log = LoggerFactory.getLogger();


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
    log.debug("burger: init drawer dispatch");
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
        log.error("Action data error", action);
        return state;
      }
      return state.withDrawer(action.setDrawer);
    }
  }
}

export { DrawerReducer, DrawerReducerDispatch };
