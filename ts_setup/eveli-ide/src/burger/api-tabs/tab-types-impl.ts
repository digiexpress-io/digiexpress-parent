import { ManyTabs, OneTab, NavHistory } from './tab-api';

interface TabProps<T> {
  id: string;
  label: string | React.ReactElement;
  icon?: string | React.ReactElement;
  data?: T;
  edit?: boolean;
}

class ImmutableTab<T> implements OneTab<T> {
  private _id: string;
  private _label: string | React.ReactElement;
  private _icon?: string | React.ReactElement;
  private _data?: T;
  private _edit?: boolean;

  constructor(props: TabProps<T>) {
    this._id = props.id;
    this._label = props.label;
    this._icon = props.icon;
    this._data = props.data;
    this._edit = props.edit;
    
  }

  get id() { return this._id; }
  get label() { return this._label; }
  get icon() { return this._icon; }
  get data() { return this._data; }
  get edit() { return this._edit; }

  withLabel(label: string | React.ReactElement) {
    return new ImmutableTab({
      id: this._id,
      label: label,
      icon: this._icon,
      data: this._data,
      edit: this._edit
    })
  }
  withIcon(icon: string | React.ReactElement | undefined) {
    return new ImmutableTab({
      id: this._id,
      label: this._label,
      icon: icon,
      data: this._data,
      edit: this._edit
    })
  }
  withData(data: T | undefined) {
    return new ImmutableTab({
      id: this._id,
      label: this._label,
      icon: this._icon,
      data: data,
      edit: this._edit
    })
  }
  withEdit(edit: boolean | undefined) {
    return new ImmutableTab({
      id: this._id,
      label: this._label,
      icon: this._icon,
      data: this._data,
      edit: edit
    })
  }
}


export class ManyTabsImpl implements ManyTabs {
  private _tabs: ImmutableTab<any>[];
  private _history: NavHistory;
  private _secondary?: string;
  private _drawer: boolean;

  constructor(props: {
    tabs?: ImmutableTab<any>[],
    history?: NavHistory,
    secondary?: string,
    drawer?: boolean
  }) {
    this._drawer = props.drawer ? true : false;
    this._secondary = props.secondary;
    this._tabs = props.tabs ? props.tabs : [];
    this._history = props.history ? props.history : { open: 0 };
  }
  get tabs(): readonly OneTab<any>[] {
    return this._tabs as any;
  }
  get history() {
    return this._history;
  }
  get secondary() {
    return this._secondary;
  }
  get drawer() {
    return this._drawer;
  }
  private next(history: NavHistory, tabs?: ImmutableTab<any>[]): ManyTabs {
    const newTabs: ImmutableTab<any>[] = tabs ? tabs : this._tabs;
    return new ManyTabsImpl({ 
      secondary: this._secondary, 
      drawer: this._drawer,
      tabs: [...newTabs], history});
  }
  withTabData(tabId: string, updateCommand: (oldData: any) => any): ManyTabs {
    const tabs: ImmutableTab<any>[] = [];
    for (const tab of this._tabs) {
      if (tabId === tab.id) {
        const newData = updateCommand(tab.data);
        tabs.push(tab.withData(newData));
      } else {
        tabs.push(tab);
      }
    }
    return this.next(this.history, tabs);
  }
  withTab(newTabOrTabIndex: OneTab<any> | number): ManyTabs {
    if (typeof newTabOrTabIndex === 'number') {
      const tabIndex = newTabOrTabIndex as number;
      return this.next({ previous: this.history, open: tabIndex });
    }
    const newTab = new ImmutableTab<any>(newTabOrTabIndex as OneTab<any>);
    const alreadyOpen = this.findTab(newTab.id);
    if (alreadyOpen !== undefined) {
      const editModeChange = this.tabs[alreadyOpen].edit !== newTab.edit;
      if (editModeChange && newTab.edit === true) {
        return this.deleteTab(newTab.id).withTab(newTab);
      }
      if (alreadyOpen === this._history.open) {
        return this;
      }
      return this.next({ previous: this.history, open: alreadyOpen });
    }

    return this.next({ previous: this.history, open: this.tabs.length }, this._tabs.concat(newTab));
  }
  findTab(newTabId: string): number | undefined {
    let index = 0;
    for (let tab of this.tabs) {
      if (tab.id === newTabId) {
        return index;
      }
      index++
    }
    return undefined;
  }
  withSecondary(newItemId?: string): ManyTabs {
    return new ManyTabsImpl({ 
      secondary: newItemId, 
      tabs: this._tabs, 
      history: this._history, 
      drawer: this._drawer });
  }
  withDrawer(open: boolean): ManyTabs {
    return new ManyTabsImpl({ 
      secondary: this._secondary, 
      tabs: this._tabs, 
      history: this._history, 
      drawer: open
    });
  }
  getTabData<T>(tabId: string): T {
    const tabIndex = this.findTab(tabId);
    if (tabIndex) {
      return this.tabs[tabIndex].data;
    }
    console.error(this);
    throw new Error(`cant find tab: '${tabId}'`);
  }
  deleteTab(tabId: string): ManyTabs {
    const tabs: ImmutableTab<any>[] = [];
    for (const tab of this._tabs) {
      if (tabId !== tab.id) {
        tabs.push(tab);
      }
    }
    const result = this.next(this.history, tabs).withTab(tabs.length - 1);
    return result;
  }

  deleteTabs(): ManyTabs {
    const tabs: ImmutableTab<any>[] = [];
    return this.next({ previous: this.history, open: 0 }, tabs);
  }
}