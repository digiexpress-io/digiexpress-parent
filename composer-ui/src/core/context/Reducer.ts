import Ide from './ide';
import Client from '../client';


enum ActionType {
  setSite = "setSite",
  setPageUpdate = "setPageUpdate",
  setPageUpdateRemove = "setPageUpdateRemove"
}

interface Action {
  type: ActionType;
  setPageUpdateRemove?: {pages: Client.ServiceDocumentId[]}
  setPageUpdate?: { page: Client.ServiceDocumentId, value: Client.AstCommand[] };
  setSite?: { site: Client.Site };
}

const ActionBuilder = {
  setPageUpdateRemove: (setPageUpdateRemove: { pages: Client.ServiceDocumentId[] } ) => ({type: ActionType.setPageUpdateRemove, setPageUpdateRemove }),
  setPageUpdate: (setPageUpdate: { page: Client.ServiceDocumentId, value: Client.AstCommand[] }) => ({ type: ActionType.setPageUpdate, setPageUpdate }),
  setSite: (setSite: { site: Client.Site }) => ({ type: ActionType.setSite, setSite })
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
    case ActionType.setPageUpdateRemove: {
      if (action.setPageUpdateRemove) {
        return state.withoutPages(action.setPageUpdateRemove.pages);
      }
      console.error("Action data error", action);
      return state;
    }
  }
}

export type { Action }
export { Reducer, ReducerDispatch, ActionType };
