
import { DialobTree } from './dialob-types';
import { HdesTree } from './hdes-types';
import { StencilTree } from './stencil-types';


export type ServiceDescriptorId = string;
export type ServiceDefinitionId = string;
export type ProjectId = string;
export type ServiceReleaseId = string;
export type ConfigType = 'STENCIL' | 'DIALOB' | 'HDES' | 'SERVICE' | 'RELEASE';
export type ClientEntityType = 'PROJECT' | 'SERVICE_DEF' | 'SERVICE_RELEASE'

export interface ProgramMessage {
  id: string, msg: string
}

export interface ClientEntity<T extends string> {
  id: T;
  version: string;
  created: string;
  updated: string;
  type: ClientEntityType;
}
export interface Project extends ClientEntity<ProjectId> {
  id: ProjectId;
  name: string;
  head: string;
  config: ProjectConfig;
  revisions: ProjectRevision[];
}
export interface ServiceDefinition extends ClientEntity<ServiceDefinitionId> {
  id: ServiceDefinitionId;
  refs: RefIdValue[]; // stencil and wrench
  projectId: string;
  descriptors: ServiceDescriptor[];
}
export interface ServiceRelease extends ClientEntity<ServiceReleaseId> {
  id: string;
}

export interface ServiceDescriptor {
  id: ServiceDescriptorId;
  name: string;
  desc: string;
  flowId: string
  formId: string;
}
export interface ProjectConfig {
  stencil: string;
  dialob: string;
  hdes: string;
  project: string;
}
export interface ProjectRevision {
  id: string;
  revisionName: string;
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



export interface HeadState {
  name: string,
  commit?: string,
  contentType: "OK" | "NOT_CREATED" | "EMPTY" | "ERRORS" | "NO_CONNECTION" | "BACKEND_NOT_FOUND",
  projects: Record<string, Project>,
  definitions: Record<string, ServiceDefinition>,
  releases: Record<string, ServiceRelease>,
}

export interface DefinitionState {
  definition: ServiceDefinition;
  dialob: DialobTree;
  stencil: StencilTree;
  hdes: HdesTree;
}

export interface ClientError {
  text: string;
  status: number;
  errors: { id: string; value: string; }[];
}

export interface CreateBuilder {
  head(): Promise<HeadState>;
  migrate(init: object): Promise<{}>;
}

export interface Client {
  config: StoreConfig;
  create(): CreateBuilder;
  head(): Promise<HeadState>
  dialob(): Promise<DialobTree>;
  definition(id: ServiceDefinitionId): Promise<DefinitionState>
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

