
import {Provider as ProviderAs, ProviderProps} from './Provider';
import {default as APIAs} from './api';


declare namespace CodeEditor {
  export type {
     ProviderProps
  }
}

namespace CodeEditor {
  export const API = APIAs;
  export const Provider = ProviderAs;
  
}


export default CodeEditor;


