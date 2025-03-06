import { StencilApi } from './StencilApi';
import {parseErrors} from './error'

export type {StencilApi};

export namespace StencilClient {

   export const service = (init: { store: StencilApi.StencilRestApi }): StencilApi.Service => {
    return { ...init.store };
  }
  
  export interface StoreError extends Error {
    text: string;
    status: number;
    errors: StencilApi.ErrorMsg[];
  }
  
  
  export class StoreErrorImpl extends Error {
    private _props: StencilApi.ErrorProps;
    constructor(props: StencilApi.ErrorProps) {
      super(props.text);
      this._props = {
        text: props.text,
        status: props.status,
        errors: parseErrors(props.errors)
      };
    }
    get name() {
      return this._props.text;
    }
    get status() {
      return this._props.status;
    }
    get errors() {
      return this._props.errors;
    }
  }
}