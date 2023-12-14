import { FlowEditorMode, FlowEditorModeProps } from "./FlowEditorMode"

declare namespace Flow {
  export { 
    FlowEditorModeProps as ProviderProps
  };
}

namespace Flow {
  export const Provider = FlowEditorMode; 
}

export default Flow;