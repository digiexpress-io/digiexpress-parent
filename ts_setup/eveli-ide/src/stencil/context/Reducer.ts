import Ide from './ide';
import {StencilApi} from '../client';


enum ActionType {
  setFilterLocale = "setFilterLocale",
  setSite = "setSite",
  setPageUpdate = "setPageUpdate",
  setPageUpdateRemove = "setPageUpdateRemove"
}

interface Action {
  type: ActionType;

  setFilterLocale?: StencilApi.LocaleId;
  setPageUpdateRemove?: {pages: StencilApi.PageId[]}
  setPageUpdate?: { page: StencilApi.PageId, value: StencilApi.LocalisedContent };
  setSite?: { site: StencilApi.Site };
}

const ActionBuilder = {
  setFilterLocale: (locale?: StencilApi.LocaleId ) => ({type: ActionType.setFilterLocale, setFilterLocale: locale }),
  setPageUpdateRemove: (setPageUpdateRemove: { pages: StencilApi.PageId[] } ) => ({type: ActionType.setPageUpdateRemove, setPageUpdateRemove }),
  setPageUpdate: (setPageUpdate: { page: StencilApi.PageId, value: StencilApi.LocalisedContent }) => ({ type: ActionType.setPageUpdate, setPageUpdate }),
  setSite: (setSite: { site: StencilApi.Site }) => ({ type: ActionType.setSite, setSite }),
}

class ReducerDispatch implements Ide.Actions {

  private _sessionDispatch: React.Dispatch<Action>;
  private _service: StencilApi.Service;
  
  constructor(session: React.Dispatch<Action>, service: StencilApi.Service) {
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
  handleLocaleFilter(locale?: StencilApi.LocaleId) {
    this._sessionDispatch(ActionBuilder.setFilterLocale(locale));
  }
  async handleLoadSite(): Promise<void> {
    return this._service.getSite().then(site => this._sessionDispatch(ActionBuilder.setSite({site})));
  }
  handlePageUpdate(page: StencilApi.PageId, value: StencilApi.LocalisedContent): void {
    this._sessionDispatch(ActionBuilder.setPageUpdate({page, value}));
  }
  handlePageUpdateRemove(pages: StencilApi.PageId[]): void {
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
