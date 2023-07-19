
export type ReleaseId = string;
export type EntityId = string;
export type FormId = string;
export type FormrevId = string;
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
  releases: Record<ReleaseId, Release>;
  revs: Record<FormrevId, FormRev>;
  forms: Record<FormId, Form>;
}

export type DocumentType = "FORM" | "FORM_REV" | "RELEASE";


export interface Entity {  
  id: EntityId; 
  version: string;
  description?: string;
  type: DocumentType;
  name: string;
  created: LocalDateTime; 
}

export interface Form extends Entity {
  data: FormBody
}

interface FormBody {
  variables: Variable[];
}

export interface Variable {
  name: string;
  expression?: string;
  defaultValue?: string;
  context?: boolean;
  published?: boolean;
  contextType?: string;
}


export interface FormRev extends Entity {
  head: string; //latest form id
  updated: LocalDateTime;    
  entries: FormRevisionEntryDocument[];
}

export interface FormRevisionEntryDocument {
  id: string;
  revisionName: string;
  formId: FormId;
  created: LocalDateTime;
  updated: LocalDateTime;
  description: string;
}

export interface Release extends Entity {
}


export interface ReleaseDump {
  id: string;
  name: string;
  description: string;
  hash: string;
  content: string; //BASE 64 GZIP JSON
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
  form(name: string): Promise<Site>;
  fill(init: InitSession): Promise<{id: string}>;
}

export interface InitSession {
  formId: string;
  language: string;
  contextValues: Record<string, string>;
}

export interface DeleteBuilder {
  release(releaseId: ReleaseId): Promise<Site>;
  form(flowId: FormId): Promise<Site>;
  formrev(formrevId: FormrevId): Promise<Site>;
}

export interface Service {
  config: StoreConfig;
  delete(): DeleteBuilder;
  create(): CreateBuilder;
  getSite(): Promise<Site>
  releaseDump(id: ReleaseId): Promise<ReleaseDump>
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

