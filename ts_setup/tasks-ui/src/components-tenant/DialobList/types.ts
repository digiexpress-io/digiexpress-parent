import { TenantEntryDescriptor, Group } from 'descriptor-tenant';
import Table from 'table';


interface DialobListTabState {
  id: number,
  label: string,
  color: string,
  group: Group,
  disabled: boolean,
  count: number | undefined
}


interface DialobListStateInit {
  activeTab: number,
  activeDialob: TenantEntryDescriptor | undefined,
  tabs: DialobListTabState[];
}

interface DialobListState extends DialobListStateInit {
  withActiveTab(activeTab: number): DialobListState;
  withActiveTask(activeTask: TenantEntryDescriptor | undefined): DialobListState;
  withTabs(tabs: DialobListTabState[]): DialobListState;
}

class ImmutableDialobListState implements DialobListState {
  private _activeTab: number;
  private _activeDialob: TenantEntryDescriptor | undefined;
  private _tabs: DialobListTabState[];

  constructor(props: DialobListStateInit) {
    this._activeTab = props.activeTab;
    this._activeDialob = props.activeDialob;
    this._tabs = props.tabs;
  }

  get activeTab() { return this._activeTab };
  get activeDialob() { return this._activeDialob };
  get tabs() { return this._tabs };
  withActiveTab(activeTab: number): DialobListState {
    return new ImmutableDialobListState({ activeTab, activeDialob: this._activeDialob, tabs: this._tabs.map((tab) => ({ ...tab, disabled: tab.id !== activeTab })) });
  }

  withActiveTask(activeDialob: TenantEntryDescriptor | undefined): DialobListState {
    return new ImmutableDialobListState({ activeDialob, activeTab: this._activeTab, tabs: this._tabs })
  }

  withTabs(tabs: DialobListTabState[]): DialobListState {
    let activeDialob: TenantEntryDescriptor | undefined;
    if (this._activeDialob) {
      activeDialob = tabs.flatMap((tab) => tab.group.records).find((descriptor) => descriptor.source.id === this._activeDialob?.source.id);
    }
    return new ImmutableDialobListState({ activeTab: this._activeTab, activeDialob, tabs });
  }

}


function getActiveTab(tabs: DialobListTabState[]): number {
  for (const tab of tabs) {
    if (tab.group.records.length) {
      return tab.id;
    }
  }
  return 0;
}


const initTable = (records: TenantEntryDescriptor[]) => new Table.TablePaginationImpl<TenantEntryDescriptor>({
  src: records,
  orderBy: 'formTitle',
  order: 'asc',
  sorted: true,
  rowsPerPage: 15,
});



function initTabs(tabs: DialobListTabState[]): DialobListState {
  const activeTab = getActiveTab(tabs);

  return new ImmutableDialobListState({
    activeTab,
    activeDialob: undefined,
    tabs
  }).withActiveTab(activeTab);
}



export type { DialobListState, DialobListTabState, DialobListStateInit };
export { initTable, initTabs }