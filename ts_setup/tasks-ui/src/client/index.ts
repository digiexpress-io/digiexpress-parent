import { ServiceImpl as ServiceImplAs } from './backend-impl';
import { DefaultStore as DefaultStoreAs } from './backend-store-impl';

import {
  BackendError, Backend, StoreConfig, Store,
} from './backend-types';

import {
  ServiceErrorMsg,
  ServiceErrorProps,
  StoreError,
  StoreErrorImpl as StoreErrorImplAs
} from './error-types';


export type {
  BackendError, Backend, StoreConfig, Store, StoreError,
}

declare namespace BackendNS {
  export type {
    BackendError, Backend, StoreConfig, Store,

    ServiceErrorMsg,
    ServiceErrorProps,
    StoreError,
  }
}
namespace BackendNS {
  export const ServiceImpl = ServiceImplAs;
  export const DefaultStore = DefaultStoreAs;
  export const StoreErrorImpl = StoreErrorImplAs;
}
export default BackendNS;