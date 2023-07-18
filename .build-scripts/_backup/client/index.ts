import AstAPI from "./AstAPI";
import ModelAPI from "./ModelAPI";
import ResourceAPI from "./ResourceAPI";
import DebugAPI from "./DebugAPI";

import ModelService from "./spi/ModelService";
import AstService from "./spi/AstService";
import ResourceService from "./spi/ResourceService";
import DebugService from "./spi/DebugService";
import { Service, Store } from "./Service";

const getErrorMsg = (error: any) => {
  if (error.msg) {
    return error.msg;
  }
  if (error.value) {
    return error.value
  }
  if (error.message) {
    return error.message;
  }
}

const getErrorId = (error: any) => {
  if (error.id) {
    return error.id;
  }
  if (error.code) {
    return error.code
  }
  return "";
}


const parseErrors = (props: any[]): Hdes.ErrorMsg[] => {
  if (!props) {
    return []
  }

  const result: Hdes.ErrorMsg[] = props.map(error => ({
    id: getErrorId(error),
    value: getErrorMsg(error)
  }));

  return result;
}

declare namespace Hdes {
  export interface ErrorMsg {
    id: string;
    value: string;
  }

  export interface ErrorProps {
    text: string;
    status: number;
    errors: any[];
  }

  export type { Service, Store, AstAPI, ModelAPI, ResourceAPI, DebugAPI };
}

namespace Hdes {
  export class StoreError extends Error {
    private _props: ErrorProps;

    constructor(props: ErrorProps) {
      super(props.text);
      this._props = {
        text: props.text,
        status: props.status,
        errors: parseErrors(props.errors)
      };


      ///children.errors
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


  export class ServiceImpl implements Hdes.Service {
    private _store: Store;
    private _models: ModelAPI.Service;
    private _debug: DebugAPI.Service;
    private _ast: AstAPI.Service;
    private _resources: ResourceAPI.Service;

    constructor(store: Hdes.Store) {
      this._store = store;
      this._models = new ModelService(store);
      this._debug = new DebugService(store);
      this._ast = new AstService(store);
      this._resources = new ResourceService(store);
    }

    get models() {
      return this._models;
    }
    get debug() {
      return this._debug;
    }
    get ast() {
      return this._ast;
    }
    get resources() {
      return this._resources;
    }
  }
}

export default Hdes;

