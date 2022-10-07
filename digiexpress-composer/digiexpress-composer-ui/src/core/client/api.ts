export type EntityId = string;
export type ProgramStatus = "UP" | "AST_ERROR" | "PROGRAM_ERROR" | "DEPENDENCY_ERROR";
export type LocalDateTime = string;

export interface AstCommand {
  
}

export interface ProgramMessage {
  id: string;
  msg: string;
}

export interface Site {
  name: string,
  contentType: "OK" | "NOT_CREATED" | "EMPTY" | "ERRORS" | "NO_CONNECTION",
}


export interface Entity {  
  id: EntityId; 
  name?: string;
  created: LocalDateTime; 
}

export interface ServiceErrorMsg {
  id: string;
  value: string;
}
export interface ServiceErrorProps {
  text: string;
  status: number;
  errors: ServiceErrorMsg[];
}

export interface CreateBuilder {
  site(): Promise<Site>;
  importData(init: string): Promise<Site>;
  release(props: {name: string, desc: string}): Promise<Site>;
}

export interface InitSession {
  formId: string;
  language: string;
  contextValues: Record<string, string>;
}

export interface DeleteBuilder {
}

export interface Service {
  config: StoreConfig;
  delete(): DeleteBuilder;
  create(): CreateBuilder;
  getSite(): Promise<Site>
  copy(id: string, name: string): Promise<Site>
}
export interface StoreConfig {
  url: string;
  oidc?: string;
  status?: string;
  csrf?: { key: string, value: string }
}
export interface Store {
  config: StoreConfig;
  fetch<T>(path: string, init?: RequestInit): Promise<T>;
}

