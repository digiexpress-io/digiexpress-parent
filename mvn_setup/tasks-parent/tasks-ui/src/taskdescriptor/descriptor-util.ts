import { parseISO, isAfter, isEqual, differenceInCalendarDays, differenceInDays } from 'date-fns';

import { Task, Profile } from 'taskclient';
import { TaskDescriptor, FilterBy, AssigneeGroupType, TeamGroupType } from './descriptor-types';
import { _nobody_, Palette } from './descriptor-constants'


export function getDaysUntilDue(task: Task, today: Date) {
  const { dueDate } = task;
  if (!dueDate) {
    return undefined;
  }
  const dueDateClean = parseISO(dueDate);
  const daysUntilDue = differenceInDays(dueDateClean, today);

  return daysUntilDue;

}

export function getTeamspaceType(task: Task, profile: Profile, today: Date): TeamGroupType | undefined {
  if (profile.roles.filter((role) => task.roles.includes(role)).length === 0) {
    return undefined;
  }

  const { dueDate } = task;
  if (!dueDate) {
    return undefined;
  }

  const dueDateClean = parseISO(dueDate);
  //const dueDateClean = new Date(dueDate);

  if (isAfter(today, dueDateClean)) {
    return "groupOverdue";
  }
  if (dueDate && differenceInCalendarDays(dueDateClean, today) <= 5) {
    return "groupDueSoon";
  }
  return "groupAvailable";
}


export function getMyWorkType(task: Task, profile: Profile, today: Date): AssigneeGroupType | undefined {
  if (!task.assigneeIds.includes(profile.userId)) {
    return undefined;
  }

  const { startDate, dueDate } = task;
  const dueDateClean = dueDate ? parseISO(dueDate) : undefined;


  if (dueDateClean && isAfter(today, dueDateClean) && task.status === 'CREATED') {
    return "assigneeOverdue";
  }
  if (startDate && isEqual(parseISO(startDate), today) && task.status === 'CREATED') {
    return "assigneeStartsToday";
  }
  if (task.status === 'IN_PROGRESS') {
    return "assigneeCurrentlyWorking";
  }

  return "assigneeOther";
}


export function applyDescFilters(desc: TaskDescriptor, filters: FilterBy[]): boolean {
  for (const filter of filters) {
    if (filter.disabled) {
      continue;
    }
    if (!applyDescFilter(desc, filter)) {
      return false;
    }
  }

  return true;
}

export function applySearchString(desc: TaskDescriptor, searchString: string): boolean {
  const description: boolean = desc.description?.toLowerCase().indexOf(searchString) > -1;
  return desc.title.toLowerCase().indexOf(searchString) > -1 || description;
}

export function applyDescFilter(desc: TaskDescriptor, filter: FilterBy): boolean {
  switch (filter.type) {
    case 'FilterByOwners': {
      for (const owner of filter.owners) {
        if (desc.assignees.length === 0 && owner === _nobody_) {
          continue;
        }
        if (!desc.assignees.includes(owner)) {
          return false;
        }
      }
      return true;
    }
    case 'FilterByRoles': {
      for (const role of filter.roles) {
        if (desc.roles.length === 0 && role === _nobody_) {
          continue;
        }
        if (!desc.roles.includes(role)) {
          return false;
        }
      }
      return true;
    }
    case 'FilterByStatus': {
      return filter.status.includes(desc.status);
    }
    case 'FilterByPriority': {
      return filter.priority.includes(desc.priority);
    }
  }
  // @ts-ignore
  throw new Error("unknow filter" + filter)
}

export function withColors<T>(input: T[]): { color: string, value: T }[] {
  const result: { color: string, value: T }[] = [];
  const colors = Object.values(Palette.colors);
  let index = 0;
  for (const value of input) {
    result.push({ value, color: colors[index] })
    if (colors.length - 1 === index) {
      index = 0;
    } else {
      index++;
    }
  }

  return result;
}