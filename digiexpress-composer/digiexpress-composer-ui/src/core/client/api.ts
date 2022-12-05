export type EntityId = string;
export type ProgramStatus = "UP" | "AST_ERROR" | "PROGRAM_ERROR" | "DEPENDENCY_ERROR";
export type DocumentType = 'SERVICE_REV' | 'SERVICE_DEF' | 'SERVICE_CONFIG' | 'SERVICE_RELEASE';
export type ConfigType = 'STENCIL' | 'DIALOB' | 'HDES' | 'SERVICE' | 'RELEASE';
export type LocalDateTime = string;

export interface AstCommand {

}

export interface SiteMigrate {
  formRevs: FormRevisionDocument[];
  forms: FormDocument[];
  services: ServiceDefinitionDocument;
  hdes: HdesState;
  stencil: StencilState;
}

export interface FormRevisionDocument {

}

export interface FormDocument {

}
export interface HdesState {

}

export interface StencilState {

}

export interface ProgramMessage {
  id: string;
  msg: string;
}
export interface ServiceDocument {
  id: string; // unique id
  version: string; // not really nullable, just in serialization
  created: string;
  updated: string;
  type: DocumentType;
}

export interface ServiceDefinitionDocument extends ServiceDocument {

}
export interface ServiceRevisionDocument extends ServiceDocument {

}
export interface ServiceReleaseDocument extends ServiceDocument {

}
export interface ServiceConfigDocument extends ServiceDocument {
  stencil: ServiceConfigValue;
  dialob: ServiceConfigValue;
  hdes: ServiceConfigValue;
  service: ServiceConfigValue;
  type: DocumentType
}

export interface ServiceConfigValue {
  id: string
  type: ConfigType;
}

export interface Site {
  name: string,
  commit?: string,
  contentType: "OK" | "NOT_CREATED" | "EMPTY" | "ERRORS" | "NO_CONNECTION" | "BACKEND_NOT_FOUND",
  revisions: Record<string, ServiceRevisionDocument>,
  definitions: Record<string, ServiceDefinitionDocument>,
  releases: Record<string, ServiceReleaseDocument>,
  configs: Record<string, ServiceConfigDocument>
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
  migrate(init: SiteMigrate): Promise<Site>;
  release(props: { name: string, desc: string }): Promise<Site>;
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
  head(): Promise<Site>
}
export interface StoreConfig {
  url: string;
  oidc?: string;
  status?: string;
  csrf?: { key: string, value: string }
}
export interface Store {
  config: StoreConfig;
  fetch<T>(path: string, init?: RequestInit & { notFound?: () => T }): Promise<T>;
}

