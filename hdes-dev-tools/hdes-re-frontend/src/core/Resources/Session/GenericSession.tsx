import { Session } from './Session';
import { Backend } from '../Backend'; 


class GenericData implements Session.Data {
  private _projects: Backend.ProjectResource[];
  private _snapshot: Backend.Snapshot; 
  
  constructor(projects?: Backend.ProjectResource[], snapshot?: Backend.Snapshot) {
    this._snapshot = snapshot ? snapshot : {};
    this._projects = projects ? projects : [];
  }
  get snapshot() : Backend.Snapshot {
    return this._snapshot;
  }
  get projects(): readonly Backend.ProjectResource[] {
    return this._projects;
  }
}


class GenericInstance implements Session.Instance {  
  private _tabs: Session.Tab<any>[];
  private _history: Session.History;
  private _dialogId?: string;
  private _search;
  private _data: Session.Data;
  private _saved: Backend.Commit[];
  private _deleted: Backend.Commit[];
  private _errors: Backend.ServerError[];
  
  constructor(
    tabs?: Session.Tab<any>[], 
    history?: Session.History, 
    dialogId?: string, 
    search?: string, 
    data?: Session.Data,
    saved?: Backend.Commit[],
    deleted?: Backend.Commit[],
    errors?: Backend.ServerError[]) {
      
    this._tabs = tabs ? tabs : [];
    this._history = history ? history : { open: 0 };
    this._dialogId = dialogId;
    this._search = search ? search : '';
    this._data = data ? data : new GenericData();
    this._saved = saved ? saved : [];
    this._deleted = deleted ? deleted : [];
    this._errors = errors ? errors : [];
  }
  get data() {
    return this._data;
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
  get saved(): readonly Backend.Commit[] {
    return this._saved;
  }
  get deleted(): readonly Backend.Commit[] {
    return this._deleted;
  }
  get errors(): readonly Backend.ServerError[] {
    return this._errors;
  }
  private next(history: Session.History, tabs?: Session.Tab<any>[]): Session.Instance {
    const newTabs = tabs ? tabs : this.tabs;
    return new GenericInstance([...newTabs], history, this.dialogId, this._search, this._data, this._saved, this._deleted, this._errors);
  }
  withErrors(newError: Backend.ServerError): Session.Instance {
    const errors = this._errors;
    errors.push(newError);
    return new GenericInstance(this._tabs, this._history, this._dialogId, this._search, this._data, this._saved, this._deleted, errors);
  }
  withSaved(newResource: Backend.Commit): Session.Instance {
    const saved = [...this._saved];
    saved.push(newResource);
    return new GenericInstance(this._tabs, this._history, this._dialogId, this._search, this._data, saved, this._deleted, this._errors);
  }
  withDeleted(deletedResource: Backend.Commit): Session.Instance {
    const deleted = [...this._deleted];
    deleted.push(deletedResource);
    return new GenericInstance(this._tabs, this._history, this._dialogId, this._search, this._data, this._saved, deleted, this._errors);
  }
  withData(init: Session.DataInit): Session.Instance {
    const snapshot = init.snapshot ? init.snapshot : this._data.snapshot;
    const projects = init.projects ? init.projects : this._data.projects;
    const newData: Session.Data = new GenericData([...projects], snapshot);
    return new GenericInstance(this._tabs, this._history, this._dialogId, this._search, newData, this._saved, this._deleted, this._errors);
  }
  withSearch(search?: string): Session.Instance {
    return new GenericInstance(this._tabs, this._history, this._dialogId, search, this._data, this._saved, this._deleted, this._errors);
  }
  withDialog(dialogId?: string): Session.Instance {
    return new GenericInstance(this._tabs, this._history, dialogId, this._search, this._data, this._saved, this._deleted, this._errors);
  }  
  withTabData(tabId: string, updateCommand: (oldData: any) => any): Session.Instance {
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
  withTab(newTabOrTabIndex: Session.Tab<any> | number): Session.Instance {
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
  getTabData<T>(tabId: string): T {
    const tabIndex = this.findTab(tabId);
    if(tabIndex) {
      return this.tabs[tabIndex].data;
    }
    console.error(this);
    throw new Error (`cant find tab: '${tabId}'`);
  }
  deleteTab(tabId: string): Session.Instance {
    const tabs: Session.Tab<any>[] = [];
    for(const tab of this.tabs) {
      if(tabId !== tab.id) {
        tabs.push(tab);
      }
    }
    return this.next(this.history, tabs).withTab(tabs.length - 1);
  }
}

const createSession = () => new GenericInstance();
export { createSession };
