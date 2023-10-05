import Client from '@taskclient';


interface MyWorkTabState {
  id: number,
  label: string,
  color: string,
  group: Client.Group,
  type: Client.AssigneeGroupType,
  disabled: boolean,
  count: number | undefined
}


interface MyWorkStateInit {
  activeTab: number,
  activeTask: Client.TaskDescriptor | undefined,
  tabs: MyWorkTabState[];
}

interface MyWorkState extends MyWorkStateInit {
  withActiveTab(activeTab: number): MyWorkState;
  withActiveTask(activeTask: Client.TaskDescriptor | undefined): MyWorkState;
}


function groupsToRecord(state: Client.Group[]): Record<Client.AssigneeGroupType, Client.Group> {
  return state.reduce((acc, item) => ({ ...acc, [item['id']]: item }), {} as Record<Client.AssigneeGroupType, Client.Group>);
}

class ImmutableTeamSpaceState implements MyWorkState {
  private _activeTab: number;
  private _activeTask: Client.TaskDescriptor | undefined;
  private _tabs: MyWorkTabState[];

  constructor(props: MyWorkStateInit) {
    this._activeTab = props.activeTab;
    this._activeTask = props.activeTask;
    this._tabs = props.tabs;
  }

  get activeTab() { return this._activeTab };
  get activeTask() { return this._activeTask };
  get tabs() { return this._tabs };

  withActiveTab(activeTab: number): MyWorkState {
    return new ImmutableTeamSpaceState({ activeTab, activeTask: this._activeTask, tabs: this._tabs.map((tab) => ({ ...tab, disabled: tab.id !== activeTab })) });
  }

  withActiveTask(activeTask: Client.TaskDescriptor | undefined): MyWorkState {
    return new ImmutableTeamSpaceState({ activeTask, activeTab: this._activeTab, tabs: this._tabs })
  }
}


function getActiveTab(tabs: MyWorkTabState[]): number {
  for (const tab of tabs) {
    if (tab.group.records.length) {
      return tab.id;
    }
  }
  return 0;
}


function getTabs(state: Client.TasksState): MyWorkTabState[] {
  const groupBy: Client.Group[] = state.withGroupBy("assignee").groups;
  const groups = groupsToRecord(groupBy);
  const assigneeOverdue = groups["assigneeOverdue"];
  const assigneeOther = groups["assigneeOther"];
  const assigneeStartsToday = groups["assigneeStartsToday"];
  const assigneeCurrentlyWorking = groups["assigneeCurrentlyWorking"];


  return [
    {
      id: 0,
      label: 'core.myWork.tab.task.currentlyWorking',
      color: Client.AssigneePalette.assigneeCurrentlyWorking,
      group: assigneeCurrentlyWorking,
      disabled: true,
      type: 'assigneeCurrentlyWorking',
      count: assigneeCurrentlyWorking.records.length
    },
    {
      id: 1,
      label: 'core.myWork.tab.task.overdue',
      color: Client.AssigneePalette.assigneeOverdue,
      group: assigneeOverdue,
      disabled: true,
      type: 'assigneeOverdue',
      count: assigneeOverdue.records.length
    },
    {
      id: 2,
      label: 'core.myWork.tab.task.startsToday',
      color: Client.AssigneePalette.assigneeStartsToday,
      group: assigneeStartsToday,
      disabled: true,
      type: 'assigneeStartsToday',
      count: assigneeStartsToday.records.length
    },
    {
      id: 3,
      label: 'core.myWork.tab.task.available',
      color: Client.AssigneePalette.assigneeOther,
      group: assigneeOther,
      disabled: true,
      type: 'assigneeOther',
      count: assigneeOther.records.length
    }
  ]
}

function init(state: Client.TasksState): MyWorkState {
  const tabs = getTabs(state);
  const activeTab = getActiveTab(tabs);

  return new ImmutableTeamSpaceState({
    activeTab,
    activeTask: undefined,
    tabs
  }).withActiveTab(activeTab);
}



export type { MyWorkState, MyWorkTabState };
export { init }