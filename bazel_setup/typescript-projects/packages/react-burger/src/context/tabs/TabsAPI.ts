
interface TabsContextType {
  session: TabsSession;
  actions: TabsActions;
}

interface TabsSession {
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

interface TabSession<T> {
  id: string;
  label: string | React.ReactElement;
  icon?: string | React.ReactElement;
  data?: T;
  edit?: boolean;
}

interface TabsHistory {
  previous?: TabsHistory;
  open: number;
}


interface TabsActions {
  handleTabAdd(newItem: TabSession<any>): void;
  handleTabData(tabId: string, updateCommand: (oldData: any) => any): void;
  handleTabChange(tabIndex: number): void;
  handleTabClose(tab: TabSession<any>): void;
  handleTabCloseCurrent(): void;
  handleTabCloseAll(): void;
}

export type { TabsContextType, TabsSession, TabSession, TabsHistory, TabsActions };