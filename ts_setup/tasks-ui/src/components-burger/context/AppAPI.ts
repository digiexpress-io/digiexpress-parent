import { SecondarySession } from './secondary/SecondaryAPI';
import { TabsSession } from './tabs/TabsAPI';


type AppId = string;

interface App<T extends Object, ContextInitProps extends Object> {
  id: AppId;
  state: [ AppStateCreate<T>, AppStateRestore<T> ]
  init: ContextInitProps 
  components: {
    toolbar: React.ElementType<ToolbarProps>;
    primary: React.ElementType<PrimaryProps>;
    tabs?: boolean | undefined;
    secondary: React.ElementType<SecondaryProps & { init?: ContextInitProps }>;
    context: React.ElementType<{init: ContextInitProps, children: React.ReactNode}>;
  }
}

type AppStateCreate<T extends Object> = (children: React.ReactNode, restorePoint?: AppState<T>) => React.ReactNode

type AppStateRestore<T> = () => { restorePoint?: T }

interface AppState<T extends Object> {
  id: AppId;
  secondary: SecondarySession;
  tabs: TabsSession;
  restorePoint: T;
}

interface AppContextType {
  session: AppSession;
  actions: AppActions;
}

interface AppSession {
  history: readonly AppState<any>[];
  active: AppId;

  withActive(active: AppId): AppSession;
  withAppState(appState: AppState<any>): AppSession;
}

interface AppActions {
  handleActive(active: AppId): void;
}


interface ToolbarProps {

}
interface PrimaryProps {

}
interface TabProps {

}
interface SecondaryProps {

}

export type { 
  AppContextType, AppSession, AppActions, TabProps,
  App, AppId, AppState, AppStateCreate, AppStateRestore
};
