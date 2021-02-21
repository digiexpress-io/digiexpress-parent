import * as React from "react";
import { Backend, InMemoryService } from './Backend';
import { Session, createSession } from './Session';
import { SessionReducer, ServiceReducer } from './Reducers';
import { ResourceContextActions, GenericResourceContextActions } from './ResourceContextActions'


type ResourceContextType = {
  session: Session.Instance;
  actions: ResourceContextActions;
}

const createService = (hdesconfig: Backend.ServerConfig | undefined) : Backend.Service => {
  if(hdesconfig?.ctx) {
    //return new ServerService(hdesconfig);  
  }
  return new InMemoryService();
}

const startService = createService(window.hdesconfig);
const startSession = createSession();

const ResourceContext = React.createContext<ResourceContextType>({
  session: startSession,
  actions: {} as ResourceContextActions
});

type ResourceProviderProps = {
  children: React.ReactNode
};

const ResourceProvider: React.FC<ResourceProviderProps> = ({ children }) => {
  const [session, sessionDispatch] = React.useReducer(SessionReducer, startSession);
  const actions: ResourceContextActions = React.useMemo(() => new GenericResourceContextActions(sessionDispatch), [sessionDispatch]);
  
  const [service] = React.useReducer(ServiceReducer, React.useMemo(() => startService.withListeners({
      onSave: (saved: Backend.Commit) => actions.handleResourceSaved(saved),
      onError: (error: Backend.ServerError) => actions.handleServerError(error),
      onDelete: (deleted: Backend.Commit) => actions.handleResourceDeleted(deleted)
    }), [actions, startService]));

  React.useEffect(() => {
    service.projects.query().onSuccess(projects => actions.handleData({projects}))
    
  }, [actions, service])
  
  return (
    <ResourceContext.Provider value={{ session, actions }}>
      {children}
    </ResourceContext.Provider>
  );
};

export { ResourceProvider, ResourceContext };