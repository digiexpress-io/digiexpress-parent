import Session from './Session';
import { Hdes } from '../../deps';


class ImmutableDebugModel implements Session.DebugModel {

  private _model: string;
  private _entity: Record<string, string>;
  private _errors?: Hdes.StoreError;
  private _output?: any;

  constructor(props: {
    model: string,
    entity: Record<string, string>,
    errors?: Hdes.StoreError,
    output?: any
  }) {
    this._model = props.model;
    this._entity = props.entity;
    this._errors = props.errors;
    this._output = props.output;
  }

  get model() {
    return this._model;
  }
  get entity() {
    return this._entity;
  }
  get errors() {
    return this._errors;
  }
  get output() {
    return this._output;
  }
  withEntity(props?: { entity: string, value: string }) {
    if (!props) {
      return new ImmutableDebugModel({ model: this._model, entity: {} });
    }
    const newEntity = Object.assign({}, this._entity);
    newEntity[props.entity] = props.value;
    return new ImmutableDebugModel({ model: this._model, errors: this._errors, output: this._output, entity: newEntity });
  }
  withOutput(output?: any) {
    return new ImmutableDebugModel({ model: this._model, errors: this._errors, entity: this._entity, output });
  }
  withErrors(errors: Hdes.StoreError) {
    return new ImmutableDebugModel({ model: this._model, entity: this._entity, output: this._output, errors });
  }
}

class SessionData implements Session.Instance {
  private _models: Record<string, ImmutableDebugModel>;
  private _active?: Session.ModelId;
  
  constructor(props: {
    models: Record<string, ImmutableDebugModel>,
    active?: Session.ModelId
  }) {
    this._models = props.models;
    this._active = props.active;
  }
  get active() {
    return this._active;
  }
  get models() {
    return Object.values(this._models);
  }
  getModel(modelId: string) {
    return this._models[modelId];
  }
  withModelDefaults(initModel: Hdes.ModelAPI.Model): Session.Instance {
    const modelId = initModel.id;
    const entity = {};
    initModel.params
      .filter(p => p.direction === "IN")
      .filter(p => p.values)
      .forEach(p => entity[p.name] = p.values);

    const newModels: Record<string, ImmutableDebugModel> = Object.assign({}, this._models);
    newModels[modelId] = new ImmutableDebugModel({ model: modelId, entity });
    return new SessionData({ models: newModels, active: initModel.id });
  }
  withActive(active: Session.ModelId): Session.Instance {
    return new SessionData({models: this._models, active});
  }
  withModel(initModel: Hdes.ModelAPI.Model): Session.Instance {
    const modelId = initModel.id;
    if (this._models[modelId]) {
      return this.withActive(modelId);
    }
    return this.withModelDefaults(initModel);
  }
  withModelEntity(props: { modelId: string, entity: string, value: string }): Session.Instance {
    if (!this._models[props.modelId]) {
      return this;
    }

    const newModels: Record<string, ImmutableDebugModel> = Object.assign({}, this._models);
    const debugData = newModels[props.modelId];
    newModels[props.modelId] = debugData.withEntity({ entity: props.entity, value: props.value });
    return new SessionData({ models: newModels, active: this._active })
  }
  withModelOutput(props: { modelId: string, output: any }): Session.Instance {
    if (!this._models[props.modelId]) {
      return this;
    }

    const newModels: Record<string, ImmutableDebugModel> = Object.assign({}, this._models);
    const debugData = newModels[props.modelId];
    newModels[props.modelId] = debugData.withOutput(props.output);
    return new SessionData({ models: newModels, active: this._active })
  }
  withModelErrors(props: { modelId: string, errors: Hdes.StoreError }): Session.Instance {
    if (!this._models[props.modelId]) {
      return this;
    }

    const newModels: Record<string, ImmutableDebugModel> = Object.assign({}, this._models);
    const debugData = newModels[props.modelId];
    newModels[props.modelId] = debugData.withErrors(props.errors);
    return new SessionData({ models: newModels, active: this._active })
  }
  withoutModelEntity(modelId: string): Session.Instance {
    if (!this._models[modelId]) {
      return this;
    }
    const newModels: Record<string, ImmutableDebugModel> = Object.assign({}, this._models);
    const debugData = newModels[modelId];
    newModels[modelId] = debugData.withEntity();
    return new SessionData({ models: newModels, active: this._active })
  }
}


export default SessionData;
