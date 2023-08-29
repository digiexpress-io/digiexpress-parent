import Client from '@taskclient';


interface TeamSpaceTabState {
  id: number,
  label: string,
  color: string,
  group: Client.Group,
  type: Client.TeamGroupType
  disabled: boolean
}


interface TeamSpaceStateInit {
  activeTask: Client.TaskDescriptor | undefined
  activeTab: number,
  tabs: TeamSpaceTabState[],
}

interface TeamSpaceState extends TeamSpaceStateInit {
  withActiveTab(activeTab: number): TeamSpaceState;
  withActiveTask(task: Client.TaskDescriptor): TeamSpaceState;
}


function groupsToRecord(state: Client.Group[]): Record<Client.TeamGroupType, Client.Group> {
  return state.reduce((acc, item) => ({ ...acc, [item['id']]: item }), {} as Record<Client.TeamGroupType, Client.Group>);
}

class ImmutableTeamSpaceState implements TeamSpaceState {

  private _tabs: TeamSpaceTabState[];
  private _activeTask: Client.TaskDescriptor | undefined;
  private _activeTab: number;

  constructor(props: TeamSpaceStateInit) {
    this._activeTask = props.activeTask;
    this._activeTab = props.activeTab;
    this._tabs = props.tabs;
  }

  get activeTask() { return this._activeTask };
  get activeTab() { return this._activeTab };
  get tabs() { return this._tabs };

  withActiveTask(task: Client.TaskDescriptor): TeamSpaceState {
    return new ImmutableTeamSpaceState({ activeTab: this._activeTab, activeTask: task, tabs: this._tabs });
  }

  withActiveTab(activeTab: number): TeamSpaceState {
    return new ImmutableTeamSpaceState({ activeTab, activeTask: this._activeTask, tabs: this._tabs.map((tab) => ({ ...tab, disabled: tab.id !== activeTab })) });
  }
}


function getActiveTab(tabs: TeamSpaceTabState[]) {

  for (const tab of tabs) {
    if (tab.group.records.length) {
      return tab.id;
    }
  }
  return 0;
}

function getTabs(state: Client.TasksState): TeamSpaceTabState[] {
  const groupBy: Client.Group[] = state.withGroupBy("team").groups;
  const groups = groupsToRecord(groupBy);
  const groupOverdue = groups['groupOverdue'];
  const groupAvailable = groups['groupAvailable'];
  const groupDueSoon = groups['groupDueSoon'];

  return [
    {
      id: 0,
      label: 'core.teamSpace.tab.task.overdue',
      type: 'groupOverdue',
      group: groupOverdue,
      color: Client.TeamGroupPallete.groupOverdue,
      disabled: true
    },
    {
      id: 1,
      label: 'core.teamSpace.tab.task.dueSoon',
      type: 'groupDueSoon',
      group: groupDueSoon,
      color: Client.TeamGroupPallete.groupDueSoon,
      disabled: true
    },
    {
      id: 2,
      label: 'core.teamSpace.tab.task.available',
      type: 'groupAvailable',
      group: groupAvailable,
      color: Client.TeamGroupPallete.groupAvailable,
      disabled: true
    },
  ];
}

function init(state: Client.TasksState): TeamSpaceState {
  const tabs = getTabs(state);
  const activeTab = getActiveTab(tabs);

  return new ImmutableTeamSpaceState({
    activeTask: undefined,
    activeTab,
    tabs,

  }).withActiveTab(activeTab);
}

export type { TeamSpaceState, TeamSpaceTabState };
export { init }