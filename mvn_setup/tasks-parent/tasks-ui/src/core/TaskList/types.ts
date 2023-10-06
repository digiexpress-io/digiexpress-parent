import Client from '@taskclient';


interface TaskListTabState {
  id: number,
  label: string,
  color: string,
  group: Client.Group,
  disabled: boolean,
  count: number | undefined
}


interface TaskListStateInit {
  activeTab: number,
  activeTask: Client.TaskDescriptor | undefined,
  tabs: TaskListTabState[];
}

interface TaskListState extends TaskListStateInit {
  withActiveTab(activeTab: number): TaskListState;
  withActiveTask(activeTask: Client.TaskDescriptor | undefined): TaskListState;
}

class ImmutableTaskListState implements TaskListState {
  private _activeTab: number;
  private _activeTask: Client.TaskDescriptor | undefined;
  private _tabs: TaskListTabState[];

  constructor(props: TaskListStateInit) {
    this._activeTab = props.activeTab;
    this._activeTask = props.activeTask;
    this._tabs = props.tabs;
  }

  get activeTab() { return this._activeTab };
  get activeTask() { return this._activeTask };
  get tabs() { return this._tabs };

  withActiveTab(activeTab: number): TaskListState {
    return new ImmutableTaskListState({ activeTab, activeTask: this._activeTask, tabs: this._tabs.map((tab) => ({ ...tab, disabled: tab.id !== activeTab })) });
  }

  withActiveTask(activeTask: Client.TaskDescriptor | undefined): TaskListState {
    return new ImmutableTaskListState({ activeTask, activeTab: this._activeTab, tabs: this._tabs })
  }
}


function getActiveTab(tabs: TaskListTabState[]): number {
  for (const tab of tabs) {
    if (tab.group.records.length) {
      return tab.id;
    }
  }
  return 0;
}


const initTable = (records: Client.TaskDescriptor[]) => new Client.TablePaginationImpl<Client.TaskDescriptor>({
  src: records,
  orderBy: 'dueDate',
  order: 'asc',
  sorted: true,
  rowsPerPage: 15,
});



function initTabs(tabs: TaskListTabState[]): TaskListState {
  const activeTab = getActiveTab(tabs);

  return new ImmutableTaskListState({
    activeTab,
    activeTask: undefined,
    tabs
  }).withActiveTab(activeTab);
}



export type { TaskListState, TaskListTabState, TaskListStateInit };
export { initTable, initTabs }