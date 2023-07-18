import { DebugExplorer } from './DebugExplorer';
import { DebugProvider, DebugContext, useContext as useContextAs, Session } from './context';


declare namespace Debug {
  export {
    Session,
  };
}

namespace Debug {
  export const Explorer = DebugExplorer;
  export const Context = DebugContext;
  export const Provider = DebugProvider;
  export const useContext = useContextAs;
}

export default Debug;

