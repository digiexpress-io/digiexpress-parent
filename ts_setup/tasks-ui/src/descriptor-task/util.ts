import { Task, UserProfileAndOrg } from 'client';

import { parseISO, isAfter, isEqual, differenceInCalendarDays, differenceInDays } from 'date-fns';

import { TaskDescriptor, FilterBy, AssigneeGroupType, TeamGroupType } from './types';
import { _nobody_ } from './constants';


export function getDaysUntilDue(task: Task, today: Date) {
  const { dueDate } = task;
  if (!dueDate) {
    return undefined;
  }
  const dueDateClean = parseISO(dueDate);
  const daysUntilDue = differenceInDays(dueDateClean, today);

  return daysUntilDue;

}

export function getTeamspaceType(task: Task, profile: UserProfileAndOrg, today: Date): TeamGroupType | undefined {
  if (profile.roles.filter((role) => task.roles.includes(role)).length === 0) {
    return undefined;
  }

  const { dueDate } = task;
  const dueDateClean = dueDate ? parseISO(dueDate) : undefined;

  if (dueDateClean && isAfter(today, dueDateClean)) {
    return "groupOverdue";
  }
  if (dueDateClean && differenceInCalendarDays(dueDateClean, today) <= 5) {
    return "groupDueSoon";
  }
  return "groupAvailable";
}


export function getMyWorkType(task: Task, profile: UserProfileAndOrg, today: Date): AssigneeGroupType | undefined {
  if (!task.assigneeIds.includes(profile.userId)) {
    return undefined;
  }

  const { startDate, dueDate } = task;
  const dueDateClean = dueDate ? parseISO(dueDate) : undefined;
  const startDateClean = startDate ? parseISO(startDate) : undefined;

  if (dueDateClean && isAfter(today, dueDateClean) && task.status === 'CREATED') {
    return "assigneeOverdue";
  }
  if (startDateClean && isEqual(startDateClean, today)) {
    return "assigneeStartsToday";
  }
  if (task.status === 'IN_PROGRESS') {
    return "assigneeCurrentlyWorking";
  }

  return "assigneeOther";
}


export function applyDescFilters(desc: TaskDescriptor, filters: readonly FilterBy[]): boolean {
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