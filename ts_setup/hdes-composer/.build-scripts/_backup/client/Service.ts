import AstAPI from "./AstAPI";
import ModelAPI from "./ModelAPI";
import ResourceAPI from "./ResourceAPI";
import DebugAPI from "./DebugAPI";

interface Service { 
  models: ModelAPI.Service;
  ast: AstAPI.Service;
  resources: ResourceAPI.Service;
  debug: DebugAPI.Service;
}

interface Store {
  fetch<T>(path: string, init?: RequestInit): Promise<T>;
}

export type {
  Service, Store
};
