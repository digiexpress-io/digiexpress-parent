import {
  ClientEntity, Project, ServiceDefinition, ServiceRelease, ServiceDescriptor, ProjectConfig, ProjectRevision,
  RefIdValue, HeadState, DefinitionState,
  ClientError, CreateBuilder, Client, StoreConfig, Store,
  ServiceDescriptorId, ServiceDefinitionId, ProjectId, ServiceReleaseId, ConfigType, ClientEntityType
} from './client-types';

import {
  DocumentId, Document, DocumentUpdate,
  TabEntity, TabBody, Tab,
  PageUpdate, Session, Actions,
} from './composer-types';

import {
  ServiceErrorMsg,
  ServiceErrorProps,
  StoreError,
  StoreErrorImpl as StoreErrorImplAs
} from './error-types';


import ErrorView from './Components/ErrorView';
import * as Context from './Components/Context';
import * as Hooks from './hooks';


declare namespace DeClient {
  export type {
    ClientEntity, Project, ServiceDefinition, ServiceRelease, ServiceDescriptor, ProjectConfig, ProjectRevision,
    RefIdValue, HeadState, DefinitionState,
    ClientError, CreateBuilder, Client, StoreConfig, Store,
    ServiceDescriptorId, ServiceDefinitionId, ProjectId, ServiceReleaseId, ConfigType, ClientEntityType
  }

  export type {
    DocumentId, Document, DocumentUpdate,
    TabEntity, TabBody, Tab,
    PageUpdate, Session, Actions
  }

  export type {
    ServiceErrorMsg,
    ServiceErrorProps,
    StoreError
  }
}


namespace DeClient {
  export const StoreErrorImpl = StoreErrorImplAs;
  export const Error = ErrorView;
  export const Provider = Context.Provider;
  export const useService = Hooks.useService;
  export const useSite = Hooks.useSite;
  export const useUnsaved = Hooks.useUnsaved;
  export const useComposer = Hooks.useComposer;
  export const useSession = Hooks.useSession;
  export const useNav = Hooks.useNav;
}

export default DeClient;