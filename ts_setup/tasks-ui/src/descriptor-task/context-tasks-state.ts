import { Task, UserProfileAndOrg } from 'client';

import { TasksState } from './types';
import { Palette, _nobody_ } from './constants';
import { TaskDescriptor, TasksPaletteType, } from './types';
import { TaskDescriptorImpl, TaskGroupsAndFiltersImpl } from './types-impl';
import { withColors } from 'components-colors';


interface ExtendedInit extends Omit<TasksState, "withProfile" | "withTasks" | "toGroupsAndFilters"> {
  owners: string[];
  roles: string[];
  palette: {
    roles: Record<string, string>
    owners: Record<string, string>
    status: Record<string, string>
    priority: Record<string, string>
  }
  profile: UserProfileAndOrg
}

class TasksStateBuilder implements TasksState {
  private _tasks: TaskDescriptor[];
  private _tasksByOwner: Record<string, TaskDescriptor[]>;
  private _owners: string[];
  private _roles: string[];
  private _palette: TasksPaletteType;
  private _profile: UserProfileAndOrg;

  constructor(init: ExtendedInit) {
    this._tasks = init.tasks;
    this._tasksByOwner = init.tasksByOwner;
    this._owners = init.owners;
    this._roles = init.roles;
    this._palette = init.palette;
    this._profile = init.profile;
  }
  get profile(): UserProfileAndOrg { return this._profile }
  get palette(): TasksPaletteType { return this._palette }
  get owners(): string[] { return this._owners }
  get roles(): string[] { return this._roles }
  get tasks(): TaskDescriptor[] { return this._tasks }
  get tasksByOwner(): Record<string, TaskDescriptor[]> { return this._tasksByOwner }

  toGroupsAndFilters(): TaskGroupsAndFiltersImpl {
    return new TaskGroupsAndFiltersImpl({
      data: this,
      filtered: this._tasks,
      filterBy: [],
      groupBy: 'status',
      groups: [],
      searchString: undefined,
    });
  }
  withProfile(profile: UserProfileAndOrg): TasksStateBuilder {
    return new TasksStateBuilder({ ...this.clone(), profile });
  }
  withTasks(input: Task[]): TasksStateBuilder {
    const tasks: TaskDescriptor[] = [];
    const roles: string[] = [_nobody_];
    const owners: string[] = [_nobody_];
    const tasksByOwner: Record<string, TaskDescriptor[]> = {};
    const today = new Date(this._profile.today);
    today.setHours(0, 0, 0, 0);

    input.forEach(task => {
      const item = new TaskDescriptorImpl(task, this._profile, today);
      tasks.push(item);

      task.roles.forEach(role => {
        if (!roles.includes(role)) {
          roles.push(role)
        }
      });
      task.assigneeIds.forEach(owner => {
        if (!owners.includes(owner)) {
          owners.push(owner)
        }

        if (!tasksByOwner[owner]) {
          tasksByOwner[owner] = [];
        }
        tasksByOwner[owner].push(item);

      });

      if (task.assigneeIds.length === 0) {
        if (!tasksByOwner[_nobody_]) {
          tasksByOwner[_nobody_] = [];
        }
        tasksByOwner[_nobody_].push(item);
      }

    });

    owners.sort();
    roles.sort();


    const palette: TasksPaletteType = {
      roles: {},
      owners: {},
      status: Palette.status,
      priority: Palette.priority
    }
    withColors(roles).forEach(e => palette.roles[e.value] = e.color);
    withColors(owners).forEach(e => palette.owners[e.value] = e.color);

    return new TasksStateBuilder({
      ...this.clone(),
      roles, owners,
      palette,
      tasks,
      tasksByOwner
    });
  }
  clone(): ExtendedInit {
    const init = this;
    return {
      profile: init.profile,
      tasks: init.tasks,
      tasksByOwner: init.tasksByOwner,
      owners: init.owners,
      roles: init.roles,
      palette: init.palette,
    }
  }
}

export { TasksStateBuilder };
