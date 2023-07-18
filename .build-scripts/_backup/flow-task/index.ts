import { FlowTaskEditor, FlowTaskEditorProps } from "./FlowTaskEditor";

declare namespace FlowTask {
  export { 
    FlowTaskEditorProps as ProviderProps
  };
}

namespace FlowTask {
  export const Provider = FlowTaskEditor; 
}

export default FlowTask;