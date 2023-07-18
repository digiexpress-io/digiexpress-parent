import { GraphTree as GraphTreeAs, GraphTreeProps } from "./GraphTree"

declare namespace FlowTree {
  export { 
    GraphTreeProps as ProviderProps
  };
}

namespace FlowTree {
  export const Provider = GraphTreeAs; 
}

export default FlowTree;