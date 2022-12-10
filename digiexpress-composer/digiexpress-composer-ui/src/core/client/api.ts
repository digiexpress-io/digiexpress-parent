export type EntityId = string;
export type ServiceRevisionValueId = string;
export type ServiceDocumentId = string;
export type ProcessValueId = string; 
export type ProgramStatus = "UP" | "AST_ERROR" | "PROGRAM_ERROR" | "DEPENDENCY_ERROR";
export type DocumentType = 'SERVICE_REV' | 'SERVICE_DEF' | 'SERVICE_CONFIG' | 'SERVICE_RELEASE';
export type ConfigType = 'STENCIL' | 'DIALOB' | 'HDES' | 'SERVICE' | 'RELEASE';
export type LocalDateTime = string;
export type EntityType = DocumentType;


export interface AstCommand {

}

export interface SiteMigrate {
  
}


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

export interface Site {
  name: string,
  commit?: string,
  contentType: "OK" | "NOT_CREATED" | "EMPTY" | "ERRORS" | "NO_CONNECTION" | "BACKEND_NOT_FOUND",
  revisions: Record<string, ServiceRevisionDocument>,
  definitions: Record<string, ServiceDefinitionDocument>,
  releases: Record<string, ServiceReleaseDocument>,
  configs: Record<string, ServiceConfigDocument>
}

export interface SiteDefinition {
  definition: ServiceDefinitionDocument;
  dialob: ComposerDialob;
  stencil: ComposerStencil;
  hdes: ComposerHdes;
}

export interface ComposerDialob { 
}
export interface ComposerStencil { 
}
export interface ComposerHdes { 
}

export interface Entity {
  id: EntityId;
  name?: string;
  type: EntityType
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

