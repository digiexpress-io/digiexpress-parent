import Ide from './ide';
import StencilClient from '../client';


enum ActionType {
  setFilterLocale = "setFilterLocale",
  setSite = "setSite",
  setPageUpdate = "setPageUpdate",
  setPageUpdateRemove = "setPageUpdateRemove"
}

interface Action {
  type: ActionType;

  setFilterLocale?: StencilClient.LocaleId;
  setPageUpdateRemove?: {pages: StencilClient.PageId[]}
  setPageUpdate?: { page: StencilClient.PageId, value: StencilClient.LocalisedContent };
  setSite?: { site: StencilClient.Site };
}

const ActionBuilder = {
  setFilterLocale: (locale?: StencilClient.LocaleId ) => ({type: ActionType.setFilterLocale, setFilterLocale: locale }),
  setPageUpdateRemove: (setPageUpdateRemove: { pages: StencilClient.PageId[] } ) => ({type: ActionType.setPageUpdateRemove, setPageUpdateRemove }),
  setPageUpdate: (setPageUpdate: { page: StencilClient.PageId, value: StencilClient.LocalisedContent }) => ({ type: ActionType.setPageUpdate, setPageUpdate }),
  setSite: (setSite: { site: StencilClient.Site }) => ({ type: ActionType.setSite, setSite }),
}

class ReducerDispatch implements Ide.Actions {

  private _sessionDispatch: React.Dispatch<Action>;
  private _service: StencilClient.Service;
  
  constructor(session: React.Dispatch<Action>, service: StencilClient.Service) {
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
  handleLocaleFilter(locale?: StencilClient.LocaleId) {
    this._sessionDispatch(ActionBuilder.setFilterLocale(locale));
  }
  async handleLoadSite(): Promise<void> {
    return this._service.getSite().then(site => this._sessionDispatch(ActionBuilder.setSite({site})));
  }
  handlePageUpdate(page: StencilClient.PageId, value: StencilClient.LocalisedContent): void {
    this._sessionDispatch(ActionBuilder.setPageUpdate({page, value}));
  }
  handlePageUpdateRemove(pages: StencilClient.PageId[]): void {
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
    case ActionType.setFilterLocale: {
      return state.withLocaleFilter(action.setFilterLocale);
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
