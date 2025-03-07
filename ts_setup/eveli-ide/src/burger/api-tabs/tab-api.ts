export interface ManyTabs {
  history: NavHistory;
  tabs: readonly OneTab<any>[];
  activeTab: OneTab<any> | undefined
  
  findTab(newTabId: string): number | undefined;
  getTabData(tabId: string): any;

  withTabData(tabId: string, updateCommand: (oldData: any) => any): ManyTabs;
  withTab(newTabOrTabIndex: OneTab<any> | number): ManyTabs;

  deleteTabs(): ManyTabs;
  deleteTab(tabId: string): ManyTabs;
}

export interface OneTab<T> {
  id: string;
  label: string | React.ReactElement;
  icon?: string | React.ReactElement;
  data?: T;
  edit?: boolean;
}

export interface NavHistory {
  previous?: NavHistory;
  open: number;
}

export interface TabsContextType {
  session: ManyTabs;

  handleTabAddAll(newItem: OneTab<any>[]): void;
  handleTabAdd(newItem: OneTab<any>): void;
  handleTabData(tabId: string, updateCommand: (oldData: any) => any): void;
  handleTabChange(tabIndex: number): void;
  handleTabClose(tab: OneTab<any>): void;
  handleTabCloseCurrent(): void;
  handleTabCloseAll(): void;
}
