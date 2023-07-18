import Session from './Session';
import { Hdes } from '../deps'

enum ActionType {
  setModels = "setModels",
  setErrors = "setErrors",
  setAsset = "setAsset",
  setEditor = "setEditor",

}

type setEditor = {
  id: string;
  saved?: boolean,
  errors?: Session.EditorError[],
  content?: (Hdes.AstAPI.FlCommand | Hdes.AstAPI.FtCommand | Hdes.AstAPI.DtCommand)[]
}

interface Action {
  type: ActionType;

  setAsset?: Hdes.ResourceAPI.Asset;
  setModels?: Hdes.ModelAPI.Models;
  setErrors?: Hdes.ModelAPI.ServerError[];
  setEditor?: setEditor;
}

const ActionBuilder = {
  setEditor: (setEditor: setEditor): Action => ({ type: ActionType.setEditor, setEditor }),
  setAsset: (setAsset: Hdes.ResourceAPI.Asset): Action => ({ type: ActionType.setAsset, setAsset }),
  setModels: (setModels: Hdes.ModelAPI.Models): Action => ({ type: ActionType.setModels, setModels }),
  setErrors: (setErrors: Hdes.ModelAPI.ServerError[]): Action => ({ type: ActionType.setErrors, setErrors }),
}

class ReducerDispatch implements Session.Actions {

  private _sessionDispatch: React.Dispatch<Action>;
  private _service: Hdes.Service;

  constructor(service: Hdes.Service, session: React.Dispatch<Action>) {
    this._sessionDispatch = session;
    this._service = service;
  }
  
  async setEditor(props: {id: string,  saved?: boolean, errors?: Session.EditorError[], content?: (Hdes.AstAPI.FlCommand | Hdes.AstAPI.FtCommand | Hdes.AstAPI.DtCommand)[] }) {
    this._sessionDispatch(ActionBuilder.setEditor(props))
  }

  getAsset(id: string): Promise<void> {
    return this._service.resources.get(id)
      .then(data => {
        this._sessionDispatch(ActionBuilder.setAsset(data))
      })
      .catch(data => {
        this._sessionDispatch(ActionBuilder.setErrors(data))
        throw new Error(data);
      });
  }
  
  createAsset(props: {name: string, serviceType: Hdes.ModelAPI.ServiceType}): Promise<Hdes.ResourceAPI.Asset> {
    return this._service.resources.create(props.name, props.serviceType)
      .then((asset: Hdes.ResourceAPI.Asset) => {
        
        return this.getModels().then(() => asset);
      })
  }
  
  copyAsset(props: {from: string, to: string}): Promise<Hdes.ResourceAPI.Asset> {
    return this._service.resources.copy(props.from, props.to)
      .then((asset: Hdes.ResourceAPI.Asset) => {
        return this.getModels().then(() => asset);
      })
  }


  saveAsset(asset: Hdes.ResourceAPI.Asset, body: string): Promise<void> {
    return this._service.resources.update(asset, body)
      .then(data => {
        this._sessionDispatch(ActionBuilder.setAsset(data));
        this.getModels();
      })
      .catch(data => {
        console.error("saving error", data)
        const errors: Session.EditorError[] = [];
        if(data.status === 500) {
          // code, value
          for(const error of data.errors) {
            errors.push({ id: error.code, msg: error.value });
          }
        }
        this._sessionDispatch(ActionBuilder.setEditor({id: asset.id, errors: errors}))
      });
  }

  getModels(): Promise<void> {
    return this._service.models.find()
      .then(data => {
        this._sessionDispatch(ActionBuilder.setModels(data))
      })
      .catch(data => {
        console.error(data)
        this._sessionDispatch(ActionBuilder.setErrors(data))
      })
  }
  setReload(): Promise<void> {
    return this.getModels();
  }
}

const Reducer = (state: Session.InstanceMutator, action: Action): Session.InstanceMutator => {
  switch (action.type) {
    case ActionType.setModels: {
      if (action.setModels) {
        return state.withModels(action.setModels);
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.setErrors: {
      if (action.setErrors) {
        return state.withErrors(action.setErrors);
      }
      return state;
    }
    case ActionType.setAsset: {
      if (action.setAsset) {
        return state.withEditor(state.editor.withContent(action.setAsset));
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.setEditor: {
      if (action.setEditor) {
        let editor = state.editor;
        let changes = false;

        if (action.setEditor.saved !== undefined) {
          changes = true;
          editor = state.editor.withSaved(action.setEditor.id, action.setEditor.saved === true)
        }
        if (action.setEditor.errors !== undefined) {
          changes = true;
          editor = state.editor.withErrors(action.setEditor.id, action.setEditor.errors)
        }
        if (action.setEditor.content !== undefined) {          
          changes = true;
          editor = state.editor
            .withSaved(action.setEditor.id, false)
            .withContentChange(action.setEditor.id, action.setEditor.content)
        }
        
        if (changes) {
          return state.withEditor(editor);
        }
        return state;
      }
      console.error("Action data error", action);
      return state;
    }
  }
}

export type { Action }
export { Reducer, ReducerDispatch, ActionType };
