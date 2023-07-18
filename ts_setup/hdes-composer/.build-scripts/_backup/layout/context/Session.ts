declare namespace Session {

  interface Configuration {
    drawer:   (props: ContextType) => ConfigurationComponent;
    header:   (props: ContextType) => Header;
    content:  (props: ContextType) => Content;
    toolbar:  (props: ContextType) => ToolbarItem[];
    badges:   (props: ContextType) => Badge[];
    search:   (props: ContextType, value: string, ref: React.RefObject<HTMLDivElement>) => Search;
  }
  
  interface ConfigurationComponent {}
  interface Header extends ConfigurationComponent {
    page?: React.ReactElement;
  }
  interface Search extends ConfigurationComponent {
    page?: React.ReactElement;
  }
  interface Content extends ConfigurationComponent {
    page: React.ReactElement;
  }
  interface Badge extends ConfigurationComponent {
    label: string;
    icon: React.ReactElement;
    onClick: () => React.ReactElement;
  }
  interface ToolbarItem extends ConfigurationComponent {
    id: string;
    icon: React.ReactNode;
    badge?: { color: string, text: string };
    enabled?: boolean;
    type: (id: string) =>
      { button: () => void } |
      { page: () => React.ReactNode } |
      { dialog: (onClose: () => void) => React.ReactNode};
  }
  
  type ToolbarItemButton = {
    button: () => void;
  }

  type ToolbarItemView = {
    page: () => React.ReactNode;
  }

  type ToolbarItemDialog = {
    dialog: (onClose: () => void) => React.ReactNode;
  }

  interface ContextType {
    session: Instance;
    actions: Actions;
  }

  interface Tab<T> {
    id: string;
    label: React.ReactElement | string;
    data?: T;
    edit?: boolean;
  }

  interface History {
    previous?: History;
    open: number;
  }
  
  interface Drawer {
    open: boolean;
    width: number;
  }

  interface Instance {
    history: History;
    dialogId?: string;
    search?: string;
    linkId?: string;
    drawer: Drawer;
    dimensions: {width: number, height: number},
    tabs: readonly Tab<any>[];
    findTabActive(): Tab<any> | undefined;
    findTab(newTabId: string): number | undefined;
    getTabData(tabId: string): any;
  }

  interface InstanceMutator extends Instance {
    withDrawer(props: {open: boolean, width?: number}): InstanceMutator;
    withDimensions(props: {width: number, height: number}): InstanceMutator;
    withSearch(keyword: string): InstanceMutator;
    withDialog(dialogId?: string): InstanceMutator;
    withLink(id?: string): InstanceMutator;
    withTabData(tabId: string, updateCommand: (oldData: any) => any): InstanceMutator;
    withTab(newTabOrTabIndex: Tab<any> | number): InstanceMutator;
    deleteTabs(): Session.InstanceMutator;
    deleteTab(tabId: string): Session.InstanceMutator;
  }

  interface Actions {
    handleDrawer(open?: boolean): void;
    handleDimensions(props: {width: number, height: number}): void;
    handleDialog(id?: string): void;
    handleLink(id?: string): void;
    handleSearch(keyword: string): void;
    handleTabAdd(newItem: Session.Tab<any>): void;
    handleTabChange(tabIndex: number): void;
    handleTabClose(tab: Session.Tab<any>): void;
    handleTabCloseAll(): void;
  }
}

export default Session;