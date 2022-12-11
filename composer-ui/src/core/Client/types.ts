export type ProgramStatus = "UP" | "AST_ERROR" | "PROGRAM_ERROR" | "DEPENDENCY_ERROR";
export type DocumentType = 'SERVICE_REV' | 'SERVICE_DEF' | 'SERVICE_CONFIG' | 'SERVICE_RELEASE';
export type ConfigType = 'STENCIL' | 'DIALOB' | 'HDES' | 'SERVICE' | 'RELEASE';

export interface ProgramMessage {
  id: string;
  msg: string;
}
export interface ServiceDocument {
  id: ServiceDocumentId; // unique id
  version: string; // not really nullable, just in serialization
  created: string;
  updated: string;
  type: DocumentType;
}

export interface ServiceRevisionValue {
  id: ServiceRevisionValueId;
  revisionName: string
  defId: string;
  created: string;
  updated: string;
}

export interface RefIdValue {
  id: string;
  tagName: string;
  repoId: string;
  type: ConfigType;
}

export interface ProcessValue {
  id: ProcessValueId;
  name: string;
  desc: string;
  flowId: string
  formId: string;
}

export interface ServiceDefinitionDocument extends ServiceDocument {
  refs: RefIdValue[];
  processes: ProcessValue[];
}
export interface ServiceRevisionDocument extends ServiceDocument {
  head: ServiceRevisionValueId;
  name: string;
  values: ServiceRevisionValue[];
  type: DocumentType;
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

export interface Project {
  name: string,
  commit?: string,
  contentType: "OK" | "NOT_CREATED" | "EMPTY" | "ERRORS" | "NO_CONNECTION" | "BACKEND_NOT_FOUND",
  revisions: Record<string, ServiceRevisionDocument>,
  definitions: Record<string, ServiceDefinitionDocument>,
  releases: Record<string, ServiceReleaseDocument>,
  configs: Record<string, ServiceConfigDocument>
}

export interface ServiceError {
  text: string;
  status: number;
  errors: { id: string; value: string; }[];
}

export interface CreateBuilder {
  project(): Promise<Project>;
  migrate(init: object): Promise<Project>;
  release(props: { name: string, desc: string }): Promise<Project>;
}

export interface Service {
  config: StoreConfig;
  create(): CreateBuilder;
  head(): Promise<Project>
  definition(id: ServiceDocumentId): Promise<SiteDefinition>
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

