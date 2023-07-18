import { LayoutContext, LayoutProvider, Session, useLayout } from "./context"
import { Reducer as LayoutReducer, ReducerDispatch as LayoutReducerDispatch } from './context/Reducer';
import { default as SessionDataAs }  from './context/SessionData';
import { Components as ComponentsAs } from './context/Components';



declare namespace Layout {
  export { Session };
}

namespace Layout { 
  export const Components = ComponentsAs;
  export const SessionData = SessionDataAs;
  export const Reducer = LayoutReducer;
  export const ReducerDispatch = LayoutReducerDispatch;
  export const Context = LayoutContext;
  export const Provider = LayoutProvider;
  export const useContext = useLayout;
}

export default Layout;