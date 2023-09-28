import Client from '@taskclient';


interface TeamSpaceTabState {
  id: number,
  label: string,
  color: string,
  group: Client.Group,
  type: Client.TeamGroupType,
  disabled: boolean,
  count: number | undefined
}


interface TeamSpaceStateInit {
  activeTab: number,
  activeTask: Client.TaskDescriptor | undefined,
  tabs: TeamSpaceTabState[];
}

interface TeamSpaceState extends TeamSpaceStateInit {
  withActiveTab(activeTab: number): TeamSpaceState;
  withActiveTask(activeTask: Client.TaskDescriptor | undefined): TeamSpaceState;
}


function groupsToRecord(state: Client.Group[]): Record<Client.TeamGroupType, Client.Group> {
  return state.reduce((acc, item) => ({ ...acc, [item['id']]: item }), {} as Record<Client.TeamGroupType, Client.Group>);
}

class ImmutableTeamSpaceState implements TeamSpaceState {
  private _activeTab: number;
  private _activeTask: Client.TaskDescriptor | undefined;
  private _tabs: TeamSpaceTabState[];

  constructor(props: TeamSpaceStateInit) {
    this._activeTab = props.activeTab;
    this._activeTask = props.activeTask;
    this._tabs = props.tabs;
  }

  get activeTab() { return this._activeTab };
  get activeTask() { return this._activeTask };
  get tabs() { return this._tabs };

  withActiveTab(activeTab: number): TeamSpaceState {
    return new ImmutableTeamSpaceState({ activeTab, activeTask: this._activeTask, tabs: this._tabs.map((tab) => ({ ...tab, disabled: tab.id !== activeTab })) });
  }

  withActiveTask(activeTask: Client.TaskDescriptor | undefined): TeamSpaceState {
    return new ImmutableTeamSpaceState({ activeTask, activeTab: this._activeTab, tabs: this._tabs })
  }
}


function getActiveTab(tabs: TeamSpaceTabState[]): number {
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
  const groupOverdue = groups["groupOverdue"];
  const groupDueSoon = groups["groupDueSoon"];
  const groupAvailable = groups["groupAvailable"];


  return [
    {
      id: 0,
      label: 'core.teamSpace.tab.task.overdue',
      color: Client.TeamGroupPallete.groupOverdue,
      group: groupOverdue,
      disabled: true,
      type: 'groupOverdue',
      count: groupOverdue.records.length
    },
    {
      id: 1,
      label: 'core.teamSpace.tab.task.dueSoon',
      color: Client.TeamGroupPallete.groupDueSoon,
      group: groupDueSoon,
      disabled: true,
      type: 'groupDueSoon',
      count: groupDueSoon.records.length

    },
    {
      id: 2,
      label: 'core.teamSpace.tab.task.available',
      color: Client.TeamGroupPallete.groupAvailable,
      group: groupAvailable,
      disabled: true,
      type: 'groupAvailable',
      count: groupAvailable.records.length
    }
  ]
}

function init(state: Client.TasksState): TeamSpaceState {
  const tabs = getTabs(state);
  const activeTab = getActiveTab(tabs);

  return new ImmutableTeamSpaceState({
    activeTab,
    activeTask: undefined,
    tabs
  }).withActiveTab(activeTab);
}



export type { TeamSpaceState, TeamSpaceTabState };
export { init }