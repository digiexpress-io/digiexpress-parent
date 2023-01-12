import { ServiceImpl as ServiceImplAs } from './client';
import { DefaultStore as DefaultStoreAs } from './client-store';

import {
  ClientEntity, Project, ServiceDefinition, ServiceRelease, ServiceDescriptor, ProjectConfig, ProjectRevision,
  RefIdValue, HeadState, DefinitionState,
  ClientError, CreateBuilder, Client, StoreConfig, Store,
  ServiceDescriptorId, ServiceDefinitionId, ProjectId, ServiceReleaseId, ConfigType, ClientEntityType, ProgramMessage
} from './client-types';

import {
  DocumentId, Document, DocumentUpdate,
  TabEntity, TabBody, Tab,
  PageUpdate, Session, Actions,
} from './composer-types';


import {
  StencilLocalizedSite,
  StencilTopic,
  StencilTopicBlob,
  StencilTopicHeading,
  StencilTopicLink,
  StencilTree
} from './stencil-types';

import {
  DialobFormDocument,
  DialobFormRevisionDocument,
  DialobFormRevisionEntryDocument,
  DialobTree,
  DialobVariable
} from './dialob-types';

import {
  HdesAstDecision,
  HdesAstFlow,
  HdesAstService,
  HdesTree
} from './hdes-types';


import {
  ServiceErrorMsg,
  ServiceErrorProps,
  StoreError,
  StoreErrorImpl as StoreErrorImplAs
} from './error-types';

import {
  DefStateAssocsImpl as DefStateAssocsImplAs,
  DefStateAssocs, DefStateDialobAssocs
} from './def-state-assocs';


import ErrorView from './Components/ErrorView';
import { ClientContextType, ComposerContextType } from './Components/Context';
import * as Context from './Components/Context';
import * as Hooks from './hooks';


declare namespace DeClient {
  export type { ClientContextType, ComposerContextType };
  export type {
    ClientEntity, Project, ServiceDefinition, ServiceRelease, ServiceDescriptor, ProjectConfig, ProjectRevision,
    RefIdValue, HeadState, DefinitionState,
    ClientError, CreateBuilder, Client, StoreConfig, Store,
    ServiceDescriptorId, ServiceDefinitionId, ProjectId, ServiceReleaseId, ConfigType, ClientEntityType,
    ProgramMessage
  }
  
  export type {
    DefStateDialobAssocs, DefStateAssocs
  }


  export type {
    DocumentId, Document, DocumentUpdate,
    TabEntity, TabBody, Tab,
    PageUpdate, Session, Actions
  }

  export type {
    StencilLocalizedSite,
    StencilTopic,
    StencilTopicBlob,
    StencilTopicHeading,
    StencilTopicLink,
    StencilTree
  }

  export type {
    DialobFormDocument,
    DialobFormRevisionDocument,
    DialobFormRevisionEntryDocument,
    DialobTree,
    DialobVariable
  }

  export type {
    HdesAstDecision,
    HdesAstFlow,
    HdesAstService,
    HdesTree
  }

  export type {
    ServiceErrorMsg,
    ServiceErrorProps,
    StoreError
  }
}


namespace DeClient {
  export const DefStateAssocsImpl = DefStateAssocsImplAs;
  export const ServiceImpl = ServiceImplAs;
  export const DefaultStore = DefaultStoreAs;
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