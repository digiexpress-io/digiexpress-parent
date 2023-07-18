
declare namespace ModelAPI {

  type ModelDirection = "IN" | "OUT";
  type ModelStatus = "OK" | "ERROR";
  type ServiceType = "FLOW" | "FLOW_TASK" | "DT" | "TAG";
  type ValueType = "TIME" | "DATE" | "DATE_TIME" | "INSTANT" |
    "INTEGER" | "LONG" | "DECIMAL" |
    "STRING" | "BOOLEAN" | "PERIOD" | "DURATION" | "PERCENT" |
    "OBJECT" | "ARRAY";
  
  type Models = Record<ServiceType, Model[]>

  interface Model {
    id: string;
    name: string;
    description?: string;
    created: number;
    modified: number;
    type : ServiceType;
    status : ModelStatus;
    errors: ModelError[];
    params: DataType[];
    associations: ModelAssociation[]; 
  }
  
  interface DataType {
    name: string;
    ref: string;
    description: string;
    valueType: string;
    required: boolean;
    values?: string;
    properties: DataType[];
    direction: ModelDirection;
  }
  
  interface ModelAssociation {
    id: string;
    name: string;
    direction: ModelDirection;
    serviceType: ServiceType;
    associationType: "ONE_TO_ONE" | "ONE_TO_MANY";
  }

  interface ModelError {
    id: string;
    message: string;
  }
  
  interface ServerAbout {
    hash: string;
  }

  interface ServerError {
    id: string;
    messages: { code: string, value: string }[];
  }
  
  interface Service {
    getAbout(): Promise<ServerAbout>;
    
    find(): Promise<Models>;
    findById(id: string): Promise<Model>;
  }
}
export default ModelAPI;