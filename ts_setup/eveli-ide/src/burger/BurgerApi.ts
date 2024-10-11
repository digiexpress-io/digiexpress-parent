


export declare namespace BurgerApi {

  export interface Release {
    id: string;
    body: {
      name: string;
      note?: string;
      created: string;
      data?: string;
    };
  }


  export type AppId = string;

  export interface App<T extends Object> {
    id: AppId;
    state: [AppStateCreate<T>, AppStateRestore<T>]
    components: {
      toolbar: React.ElementType<ToolbarProps>;
      primary: React.ElementType<PrimaryProps>;
      secondary: React.ElementType<SecondaryProps>;
    }
  }

  export type AppStateCreate<T extends Object> = (children: React.ReactNode, restorePoint?: AppState<T>) => React.ReactNode

  export type AppStateRestore<T> = () => { restorePoint?: T }

  export interface AppState<T extends Object> {
    id: AppId;
    secondary: SecondarySession;
    tabs: TabsSession;
    restorePoint: T;
  }

  export interface AppContextType {
    session: AppSession;
    actions: AppActions;
  }

  export interface AppSession {
    history: readonly AppState<any>[];
    active: AppId;

    withActive(active: AppId): AppSession;
    withAppState(appState: AppState<any>): AppSession;
  }

  export interface AppActions {
    handleActive(active: AppId): void;
  }


  export interface ToolbarProps {

  }
  export interface PrimaryProps {

  }
  export interface SecondaryProps {

  }


  export interface SecondaryContextType {
    session: SecondarySession;
    actions: SecondaryActions;
  }

  export interface SecondarySession {
    appId: string;
    secondary?: string;
    withSecondary(newItemId?: string): SecondarySession;
  }


  export interface SecondaryActions {
    handleSecondary(newItemId?: string): void;
  }



  export interface TabsContextType {
    session: TabsSession;
    actions: TabsActions;
  }

  export interface TabsSession {
    appId: string;
    history: TabsHistory;
    tabs: readonly TabSession<any>[];

    findTab(newTabId: string): number | undefined;
    getTabData(tabId: string): any;

    withTabData(tabId: string, updateCommand: (oldData: any) => any): TabsSession;
    withTab(newTabOrTabIndex: TabSession<any> | number): TabsSession;

    deleteTabs(): TabsSession;
    deleteTab(tabId: string): TabsSession;
  }

  export interface TabSession<T> {
    id: string;
    label: string | React.ReactElement;
    icon?: string | React.ReactElement;
    data?: T;
    edit?: boolean;
  }

  export interface TabsHistory {
    previous?: TabsHistory;
    open: number;
  }


  export interface TabsActions {
    handleTabAdd(newItem: TabSession<any>): void;
    handleTabData(tabId: string, updateCommand: (oldData: any) => any): void;
    handleTabChange(tabIndex: number): void;
    handleTabClose(tab: TabSession<any>): void;
    handleTabCloseCurrent(): void;
    handleTabCloseAll(): void;
  }


  interface DrawerContextType {
    session: DrawerSession;
    actions: DrawerActions;
  }

  interface DrawerSession {
    drawer: boolean;
    withDrawer(open: boolean): DrawerSession;
  }

  interface DrawerActions {
    handleDrawerOpen(open: boolean): void;
  }
}