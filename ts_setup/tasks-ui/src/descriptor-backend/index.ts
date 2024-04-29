import * as store from './backend-store-impl';
import * as ctx from './backend-ctx';
import {useBackend, BackendProvider}  from './backend-ctx';

import {
  BackendError, Backend, StoreConfig, Store, Health
} from './backend-types';

import {
  ServiceErrorMsg,
  ServiceErrorProps,
  StoreError,
  StoreErrorImpl as StoreErrorImplAs
} from './error-types';

export { useBackend, BackendProvider }
export type {
  BackendError, Backend, StoreConfig, Store,
  ServiceErrorMsg, ServiceErrorProps, StoreError, Health
}

declare namespace BackendNS {
  export type {
    BackendError, Backend, StoreConfig, Store,
    ServiceErrorMsg, ServiceErrorProps, StoreError,
  }
}
namespace BackendNS {
  export const useBackend = ctx.useBackend;
  export const BackendProvider = ctx.BackendProvider;
  export const BackendImpl = ctx.BackendImpl;
  export const BackendStoreImpl = store.BackendStoreImpl;
  export const StoreErrorImpl = StoreErrorImplAs;
}
export default BackendNS;