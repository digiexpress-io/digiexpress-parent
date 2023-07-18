import { Hdes } from '../../deps';


declare namespace Session {

  type ModelId = string;

  interface ContextType {
    active?: Active;
    session: Instance;
    service: Hdes.Service;
    actions: Actions;
  }
  
  interface Active {
    debug: Session.DebugModel, 
    model: Hdes.ModelAPI.Model
  }
  
  interface Actions {
    handleInputs(active: boolean): Promise<void>;
    handleOutputs(active: boolean): Promise<void>;
    handleSetModel(model: Hdes.ModelAPI.Model): Promise<void>;
    handleSetModelEntityDefaults(model: Hdes.ModelAPI.Model): Promise<void>
    handleSetModelEntity(props: { modelId: ModelId, entity: string, value: string }): Promise<void>
    handleSetModelErrors(props: { modelId: ModelId, errors: Hdes.StoreError }): Promise<void>
    handleExecute(props: { id: string, type: Hdes.ModelAPI.ServiceType, input: string }): Promise<void>
  }

  interface Instance {
    models: DebugModel[];
    active?: ModelId; //model id
    inputs: boolean;
    outputs: boolean;
    
    getModel(modelId: ModelId): DebugModel | undefined;
    withActive(modelId: ModelId): Instance;
    withInputs(active: boolean): Instance;
    withOutputs(active: boolean): Instance;
    withModel(model: Hdes.ModelAPI.Model): Instance;
    withModelDefaults(initModel: Hdes.ModelAPI.Model): Instance;
    withModelErrors(props: { modelId: ModelId, errors: Hdes.StoreError }): Instance;
    withModelEntity(props: { modelId: ModelId, entity: string, value: string }): Instance;
    withModelOutput(props: { modelId: ModelId, output: any }): Instance;
    withoutModelEntity(modelId: ModelId): Instance;
  }

  interface DebugModel {
    model: ModelId;
    entity: Record<string, string>
    output?: any;
    errors?: Hdes.StoreError;
    withOutput(output: any): DebugModel;
    withErrors(props: Hdes.StoreError): DebugModel;
    withEntity(props: { entity: string, value: string }): DebugModel;
  }
}

export default Session;