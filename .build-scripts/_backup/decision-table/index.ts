import { DecisionTable as DecisionTableAs, DecisionTableProps } from "./DecisionTable";


declare namespace DecisionTable {
  export { 
    DecisionTableProps as ProviderProps
  };
}

namespace DecisionTable {
  export const Provider = DecisionTableAs; 
}

export default DecisionTable;