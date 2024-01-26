import * as API from './TabsAPI';

import LoggerFactory from 'logger';
const log = LoggerFactory.getLogger();


enum ReducerActionType {
  addTab = "addTab",
  removeTab = "removeTab",
  changeTab = "changeTab",
  closeTabs = "closeTabs",
  closeTabCurrent = "closeTabCurrent",
  setTabData = "setTabData",
}

interface ReducerAction {
  type: ReducerActionType;
  addTab?: API.TabSession<any>;
  removeTab?: string;
  changeTab?: number;
  setTabData?: { id: string, updateCommand: (oldData: any) => any };
}

class TabsReducerDispatch implements API.TabsActions {

  private _sessionDispatch: React.Dispatch<ReducerAction>;
  constructor(session: React.Dispatch<ReducerAction>) {
    log.debug("burger: init tabs dispatch");
    this._sessionDispatch = session;
  }
  handleTabAdd(newItem: API.TabSession<any>) {
    this._sessionDispatch({ type: ReducerActionType.addTab, addTab: newItem });
  }
  handleTabChange(tabIndex: number) {
    this._sessionDispatch({ type: ReducerActionType.changeTab, changeTab: tabIndex })
  }
  handleTabClose(tab: API.TabSession<any>) {
    this._sessionDispatch({ type: ReducerActionType.removeTab, removeTab: tab.id });
  }
  handleTabCloseAll() {
    this._sessionDispatch({ type: ReducerActionType.closeTabs });
  }
  handleTabData(tabId: string, updateCommand: (oldData: any) => any) {
    this._sessionDispatch({
      type: ReducerActionType.setTabData,
      setTabData: { id: tabId, updateCommand }
    });
  }
  handleTabCloseCurrent() {
    this._sessionDispatch({ type: ReducerActionType.closeTabCurrent });
  }
}

const TabsReducer = (state: API.TabsSession, action: ReducerAction): API.TabsSession => {
  switch (action.type) {
    case ReducerActionType.addTab: {
      if (action.addTab) {
        return state.withTab(action.addTab);
      }
      console.error("Action data error", action);
      return state;
    }
    case ReducerActionType.changeTab: {
      if (action.changeTab === undefined) {
        console.error("Action data error", action);
        return state;
      }
      return state.withTab(action.changeTab);
    }
    case ReducerActionType.removeTab: {
      if (!action.removeTab) {
        console.error("Action data error", action);
        return state;
      }
      return state.deleteTab(action.removeTab);
    }
    case ReducerActionType.setTabData: {
      if (!action.setTabData) {
        console.error("Action data error", action);
        return state;
      }
      return state.withTabData(action.setTabData.id, action.setTabData.updateCommand);
    }
    case ReducerActionType.closeTabs: {
      return state.deleteTabs();
    }
    case ReducerActionType.closeTabCurrent: {
      const active = state.history.open;
      const tab = state.tabs[active];
      return state.deleteTab(tab.id);
    }
  }
}

export { TabsReducer, TabsReducerDispatch };
