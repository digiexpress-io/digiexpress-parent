import { TenantEntryDescriptor, Group, GroupBy, TenantState } from 'descriptor-tenant';
import Table from 'table';
import { AssigneePalette } from 'descriptor-task';


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


function groupsToRecord(state: Group[]): Record<GroupBy, Group> {
  return state.reduce((acc, item) => ({ ...acc, [item['id']]: item }), {} as Record<GroupBy, Group>);
}

function createTabs(groupBy: Group[]): DialobListTabState[] | any {
  const groups = groupsToRecord(groupBy);
  const none = groups["none"];

  return [
    {
      id: 0,
      label: 'core.myWork.tab.task.currentlyWorking',
      color: AssigneePalette.assigneeCurrentlyWorking,
      group: none,
      disabled: true,
      count: undefined
    },
  ]
}




export type { DialobListState, DialobListTabState, DialobListStateInit };
export { initTable, initTabs, createTabs }