import Ide from './ide';
import Client from '../client';


enum ActionType {
  setDebugUpdate = "setDebugUpdate",
  setSite = "setSite",
  setPageUpdate = "setPageUpdate",
  setPageUpdateRemove = "setPageUpdateRemove",
  setBranchUpdate = "setBranchUpdate"
}

interface Action {
  type: ActionType;
  setPageUpdateRemove?: {pages: Client.EntityId[]}
  setPageUpdate?: { page: Client.EntityId, value: Client.AstCommand[] };
  setSite?: { site: Client.Site };
  setDebugUpdate?: Ide.DebugSession;
  setBranchUpdate?: string;
}

const ActionBuilder = {
  setPageUpdateRemove: (setPageUpdateRemove: { pages: Client.EntityId[] } ) => ({type: ActionType.setPageUpdateRemove, setPageUpdateRemove }),
  setPageUpdate: (setPageUpdate: { page: Client.EntityId, value: Client.AstCommand[] }) => ({ type: ActionType.setPageUpdate, setPageUpdate }),
  setSite: (setSite: { site: Client.Site }) => ({ type: ActionType.setSite, setSite }),
  setDebugUpdate: (setDebugUpdate: Ide.DebugSession) => ({ type: ActionType.setDebugUpdate, setDebugUpdate }),
  setBranchUpdate: (setBranchUpdate?: string) => ({ type: ActionType.setBranchUpdate, setBranchUpdate })
}

class ReducerDispatch implements Ide.Actions {

  private _sessionDispatch: React.Dispatch<Action>;
  private _service: Client.Service;
  
  constructor(session: React.Dispatch<Action>, service: Client.Service) {
    this._sessionDispatch = session;
    this._service = service;
  }
  async handleLoad(): Promise<void> {
    return this._service.getSite()
      .then(site => {
        if(site.contentType === "NOT_CREATED") {
          this._service.create().site().then(created => this._sessionDispatch(ActionBuilder.setSite({site: created})));
        } else {
          this._sessionDispatch(ActionBuilder.setSite({site})) 
        }
      });
  }
  async handleLoadSite(site?: Client.Site): Promise<void> {
    if(site) {
      return this._sessionDispatch(ActionBuilder.setSite({site}));  
    } else {
      return this._service.getSite().then(site => this._sessionDispatch(ActionBuilder.setSite({site})));  
    }
  }
  handleBranchUpdate(branchName?: string): void {
    this._sessionDispatch(ActionBuilder.setBranchUpdate(branchName));
  }
  handleDebugUpdate(debug: Ide.DebugSession): void {
    this._sessionDispatch(ActionBuilder.setDebugUpdate(debug));
  }
  handlePageUpdate(page: Client.EntityId, value: Client.AstCommand[]): void {
    this._sessionDispatch(ActionBuilder.setPageUpdate({page, value}));
  }
  handlePageUpdateRemove(pages: Client.EntityId[]): void {
    this._sessionDispatch(ActionBuilder.setPageUpdateRemove({pages}));
  }
}

const Reducer = (state: Ide.Session, action: Action): Ide.Session => {
  switch (action.type) {
    case ActionType.setSite: {
      if (action.setSite) {
        console.log("new site", action.setSite.site);
        return state.withSite(action.setSite.site);
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.setPageUpdate: {
      if (action.setPageUpdate) {
        return state.withPageValue(action.setPageUpdate.page, action.setPageUpdate.value);
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.setDebugUpdate: {
      if (action.setDebugUpdate) {
        return state.withDebug(action.setDebugUpdate);
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.setPageUpdateRemove: {
      if (action.setPageUpdateRemove) {
        return state.withoutPages(action.setPageUpdateRemove.pages);
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.setBranchUpdate: {
      if (action.setBranchUpdate) {
        return state.withBranch(action.setBranchUpdate);
      }
      console.error("Action data error", action);
      return state;
    }
  }
}

export type { Action }
export { Reducer, ReducerDispatch, ActionType };
