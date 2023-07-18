import Session from './Session';
import { Hdes } from '../../deps'

enum ActionType {
  setModel = "setModel",
  setModelEntityDefaults = "setModelEntityDefaults",
  setModelEntity = "setModelEntity",
  setModelErrors = "setModelErrors",
  setModelOutput = "setModelOutput",
  setOutputs = "setOutputs",
  setInputs = "setInputs",
}

interface Action {
  type: ActionType;

  setOutputs?: boolean;
  setInputs?: boolean;
  setModel?: Hdes.ModelAPI.Model;
  setModelEntityDefaults?: Hdes.ModelAPI.Model;
  setModelEntity?: { modelId: Session.ModelId, entity: string, value: string };
  setModelErrors?: { modelId: Session.ModelId, errors: Hdes.StoreError };
  setModelOutput?: { modelId: Session.ModelId, output: any, errors?: Hdes.StoreError };
}

const ActionBuilder = {
  setInputs: (setInputs: boolean): Action => ({ type: ActionType.setInputs, setInputs }),
  setOutputs: (setOutputs: boolean): Action => ({ type: ActionType.setOutputs, setOutputs }),
  setModel: (setModel: Hdes.ModelAPI.Model): Action => ({ type: ActionType.setModel, setModel }),
  setModelEntityDefaults: (setModelEntityDefaults: Hdes.ModelAPI.Model): Action => ({ type: ActionType.setModelEntityDefaults, setModelEntityDefaults }),
  setModelEntity: (setModelEntity: { modelId: Session.ModelId, entity: string, value: string }): Action => ({ type: ActionType.setModelEntity, setModelEntity }),
  setModelErrors: (setModelErrors: { modelId: Session.ModelId, errors: Hdes.StoreError }): Action => ({ type: ActionType.setModelErrors, setModelErrors }),
  setModelOutput: (setModelOutput: { modelId: Session.ModelId, output: any, errors?: Hdes.StoreError }): Action => ({ type: ActionType.setModelOutput, setModelOutput }),
}

class ReducerDispatch implements Session.Actions {

  private _sessionDispatch: React.Dispatch<Action>;
  private _service: Hdes.Service;

  constructor(service: Hdes.Service, session: React.Dispatch<Action>) {
    this._sessionDispatch = session;
    this._service = service;
  }

  async handleSetModel(model: Hdes.ModelAPI.Model) {
    this._sessionDispatch(ActionBuilder.setModel(model))
  }
  async handleSetModelEntityDefaults(model: Hdes.ModelAPI.Model) {
    this._sessionDispatch(ActionBuilder.setModelEntityDefaults(model))
  }
  async handleSetModelEntity(props: { modelId: Session.ModelId, entity: string, value: string }) {
    this._sessionDispatch(ActionBuilder.setModelEntity(props));
  }
  async handleSetModelErrors(props: { modelId: Session.ModelId, errors: Hdes.StoreError }) {
    this._sessionDispatch(ActionBuilder.setModelErrors(props));
  }
  async handleInputs(active: boolean) {
    this._sessionDispatch(ActionBuilder.setInputs(active));
  }
  async handleOutputs(active: boolean) {
    this._sessionDispatch(ActionBuilder.setOutputs(active));
  }
  handleExecute(props: { id: string, type: Hdes.ModelAPI.ServiceType, input: string }): Promise<void> {
    return this._service.debug.getDebug(props)
      .then(output => {
        this._sessionDispatch(ActionBuilder.setModelOutput({ modelId: props.id, output, errors: undefined }))
      })
      .catch(errors => {
        console.log(errors);
        this._sessionDispatch(ActionBuilder.setModelErrors({ modelId: props.id, errors }))
      });
  }
}

const Reducer = (state: Session.Instance, action: Action): Session.Instance => {
  switch (action.type) {
    case ActionType.setModel: {
      if (action.setModel) {
        return state.withModel(action.setModel);
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.setModelEntity: {
      if (action.setModelEntity) {
        return state.withModelEntity(action.setModelEntity);
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.setModelEntityDefaults: {
      if (action.setModelEntityDefaults) {
        return state.withModelDefaults(action.setModelEntityDefaults);
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.setModelErrors: {
      if (action.setModelErrors) {
        return state.withModelErrors(action.setModelErrors);
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.setInputs: {
      if (action.setInputs !== undefined) {
        return state.withInputs(action.setInputs);
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.setOutputs: {
      if (action.setOutputs !== undefined) {
        return state.withOutputs(action.setOutputs);
      }
      console.error("Action data error", action);
      return state;
    }
    case ActionType.setModelOutput: {
      if (action.setModelOutput) {
        const newState = state.withModelOutput(action.setModelOutput);
        if (Object.keys(action.setModelOutput).includes("errors")) {
          return newState.withModelErrors({ modelId: action.setModelOutput.modelId, errors: action.setModelOutput.errors as any });
        }
        return newState;
      }
      console.error("Action data error", action);
      return state;
    }
  }
}

export type { Action }
export { Reducer, ReducerDispatch, ActionType };
