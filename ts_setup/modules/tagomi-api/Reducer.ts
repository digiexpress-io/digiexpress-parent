
import { TagomiApi } from './TagomiApi';
import { TagomiComposerApi } from './ide';

enum ActionType {
  setSite = "setSite",
  setTemplateUpdate = "setTemplateUpdate",
  setTemplateUpdateRemove = "setTemplateUpdateRemove"
}

interface Action {
  type: ActionType;

  setTemplateUpdateRemove?: {pages: TagomiApi.TemplateId[]}
  setTemplateUpdate?: { template: TagomiApi.TemplateId, value: TagomiApi.LocalisedContent };
  setSite?: { site: TagomiApi.TagomiContainer };
}

const ActionBuilder = {
  setTemplateUpdateRemove: (setTemplateUpdateRemove: { pages: TagomiApi.TemplateId[] } ) => ({type: ActionType.setTemplateUpdateRemove, setTemplateUpdateRemove }),
  setTemplateUpdate: (setTemplateUpdate: { template: TagomiApi.TemplateId, value: TagomiApi.LocalisedContent }) => ({ type: ActionType.setTemplateUpdate, setTemplateUpdate }),
  setSite: (setSite: { site: TagomiApi.TagomiContainer }) => ({ type: ActionType.setSite, setSite }),
}

class ReducerDispatch implements TagomiComposerApi.Actions {

  private _sessionDispatch: React.Dispatch<Action>;
  private _service: TagomiApi.Backend;
  
  constructor(session: React.Dispatch<Action>, service: TagomiApi.Backend) {
    this._sessionDispatch = session;
    this._service = service;
  }
  async handleLoad(): Promise<void> {
    return this._service.getSites()
      .then((site) => {
        this._sessionDispatch(ActionBuilder.setSite({ site })) 
      });
  }
  async handleLoadSite(): Promise<void> {
    return this._service.getSites().then((site) => this._sessionDispatch(ActionBuilder.setSite({ site })));
  }
  handleTemplateUpdate(template: TagomiApi.TemplateId, value: TagomiApi.LocalisedContent): void {
    this._sessionDispatch(ActionBuilder.setTemplateUpdate({template, value}));
  }
  handleTemplateUpdateRemove(pages: TagomiApi.TemplateId[]): void {
    this._sessionDispatch(ActionBuilder.setTemplateUpdateRemove({ pages }));
  }
}

const Reducer = (state: TagomiComposerApi.Session, action: Action): TagomiComposerApi.Session => {
  switch (action.type) {
    case ActionType.setSite: {
      if (action.setSite) {
        console.log("new site", action.setSite.site);
        return state.withSite(action.setSite.site);
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.setTemplateUpdate: {
      if (action.setTemplateUpdate) {
        return state.withTemplateValue(action.setTemplateUpdate.template, action.setTemplateUpdate.value);
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.setTemplateUpdateRemove: {
      if (action.setTemplateUpdateRemove) {
        return state.withoutTemplates(action.setTemplateUpdateRemove.pages);
      }
      console.error("Action data error", action);
      return state;
    }
  }
}

export type { Action }
export { Reducer, ReducerDispatch, ActionType };
