import Client from '@taskclient';
import Context from 'context';


interface TaskListTabState {
  id: number,
  label: string,
  color: string,
  group: Context.Group,
  disabled: boolean,
  count: number | undefined
}


interface TaskListStateInit {
  activeTab: number,
  activeTask: Context.TaskDescriptor | undefined,
  tabs: TaskListTabState[];
}

interface TaskListState extends TaskListStateInit {
  withActiveTab(activeTab: number): TaskListState;
  withActiveTask(activeTask: Context.TaskDescriptor | undefined): TaskListState;
  withTabs(tabs: TaskListTabState[]): TaskListState;
}

class ImmutableTaskListState implements TaskListState {
  private _activeTab: number;
  private _activeTask: Context.TaskDescriptor | undefined;
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

  withActiveTask(activeTask: Context.TaskDescriptor | undefined): TaskListState {
    return new ImmutableTaskListState({ activeTask, activeTab: this._activeTab, tabs: this._tabs })
  }

  withTabs(tabs: TaskListTabState[]): TaskListState {
    let activeTask: Context.TaskDescriptor | undefined;
    if (this._activeTask) {
      activeTask = tabs.flatMap((tab) => tab.group.records).find((descriptor) => descriptor.id === this._activeTask?.id);
    }
    return new ImmutableTaskListState({ activeTab: this._activeTab, activeTask, tabs });
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


const initTable = (records: Context.TaskDescriptor[]) => new Client.TablePaginationImpl<Context.TaskDescriptor>({
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