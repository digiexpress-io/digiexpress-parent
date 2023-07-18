import Session from './Session';
 

class SessionData implements Session.InstanceMutator {  
  private _tabs: Session.Tab<any>[];
  private _history: Session.History;
  private _dialogId?: string;
  private _linkId?: string;
  private _dimensions: { width: number, height: number };
  private _drawer: Session.Drawer;
  private _search: string;
  
  constructor(props: {
      dimensions: {width: number, height: number },
      drawer: Session.Drawer,
      tabs?: Session.Tab<any>[],
      history?: Session.History, 
      dialogId?: string,
      linkId?: string,
      search?: string}) {
    
    this._drawer = props.drawer; 
    this._tabs = props.tabs ? props.tabs : [];
    this._history = props.history ? props.history : { open: 0 };
    this._dialogId = props.dialogId;
    this._linkId = props.linkId;
    this._dimensions = { width: props.dimensions.width, height: props.dimensions.height };
    this._search = props.search ? props.search : '';
  }
  get linkId() {
    return this._linkId;
  }
  get search() {
    return this._search;
  }
  get tabs(): readonly Session.Tab<any>[] {
    return this._tabs;
  }
  get history() {
    return this._history;
  }
  get dialogId() {
    return this._dialogId;
  }
  get dimensions() {
    return this._dimensions;
  }
  get drawer() {
    return this._drawer;
  }
  private next(history: Session.History, tabs?: Session.Tab<any>[]): Session.InstanceMutator {
    const newTabs = tabs ? tabs : this.tabs;
    return new SessionData({drawer: this._drawer, dimensions: this._dimensions, dialogId: this.dialogId, linkId: this._linkId, search: this._search, tabs: [...newTabs], history});
  }
  withSearch(search?: string): Session.InstanceMutator {
    return new SessionData({drawer: this._drawer, dimensions: this._dimensions, tabs: this._tabs, history: this._history, dialogId: this._dialogId, linkId: this._linkId, search});
  }
  withDialog(dialogId?: string): Session.InstanceMutator {
    return new SessionData({drawer: this._drawer, dimensions: this._dimensions, tabs: this._tabs, history: this._history, linkId: this._linkId, search: this._search, dialogId});
  }  
  withLink(linkId?: string): Session.InstanceMutator {
    return new SessionData({drawer: this._drawer, dimensions: this._dimensions, tabs: this._tabs, history: this._history, dialogId: this._dialogId, search: this._search, linkId});
  }  
  withDimensions(dimensions: {width: number, height: number}): Session.InstanceMutator {
    return new SessionData({
      dimensions: { width: dimensions.width, height: dimensions.height},
      drawer: this._drawer, 
      tabs: this._tabs, history: this._history, dialogId: this._dialogId, search: this._search, linkId: this._linkId});
  }
  withDrawer(drawer: { open: boolean, width?: number }): Session.InstanceMutator {
    return new SessionData({
      drawer: { width: drawer.width ? drawer.width : this._drawer.width, open: drawer.open}, 
      dimensions: this._dimensions, tabs: this._tabs, history: this._history, dialogId: this._dialogId, search: this._search, linkId: this._linkId});
  }
  withTabData(tabId: string, updateCommand: (oldData: any) => any): Session.InstanceMutator {
    const tabs: Session.Tab<any>[] = [];
    for(const tab of this.tabs) {
      if(tabId === tab.id) {
        tabs.push({id: tab.id, label: tab.label, data: updateCommand(tab.data)});
      } else {
        tabs.push(tab);
      }
    }
    return this.next(this.history, tabs);
  }
  withTab(newTabOrTabIndex: Session.Tab<any> | number): Session.InstanceMutator {
    if(typeof newTabOrTabIndex === 'number') {
      const tabIndex = newTabOrTabIndex as number;
      return this.next({ previous: this.history, open: tabIndex });
    }
    
    const newTab = newTabOrTabIndex as Session.Tab<any>;
    const alreadyOpen = this.findTab(newTab.id);
    if(alreadyOpen !== undefined) {      
      const editModeChange = this.tabs[alreadyOpen].edit !== newTab.edit;
      if(editModeChange && newTab.edit === true) {
        return this.deleteTab(newTab.id).withTab(newTab);
      }      
      return this.next({ previous: this.history, open: alreadyOpen });
    }

    return this.next({ previous: this.history, open: this.tabs.length}, this.tabs.concat(newTab));
  }
  findTab(newTabId: string): number | undefined {
    let index = 0; 
    for(let tab of this.tabs) {
      if(tab.id === newTabId) {
        return index;
      }
      index++
    }
    return undefined;
  }
  findTabActive(): Session.Tab<any> | undefined {
    const tabs = this._tabs;
    if(tabs.length === 0) {
      return undefined;
    }
    const active = tabs[this._history.open];
    if(active) {
      return active;
    }
    return undefined;
  }
  getTabData<T>(tabId: string): T {
    const tabIndex = this.findTab(tabId);
    if(tabIndex) {
      return this.tabs[tabIndex].data;
    }
    console.error(this);
    throw new Error (`cant find tab: '${tabId}'`);
  }
  deleteTab(tabId: string): Session.InstanceMutator {
    const tabs: Session.Tab<any>[] = [];
    for(const tab of this.tabs) {
      if(tabId !== tab.id) {
        tabs.push(tab);
      }
    }
    return this.next(this.history, tabs).withTab(tabs.length - 1);
  }
  
  deleteTabs(): Session.InstanceMutator {
    const tabs: Session.Tab<any>[] = [];
    return this.next({ previous: this.history, open: 0}, tabs);
  }
}

export default SessionData;
