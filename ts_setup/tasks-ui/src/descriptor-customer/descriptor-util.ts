
import { CustomerCaseDescriptor, FilterBy } from './descriptor-types';
import { _nobody_, Palette } from './descriptor-constants'


export function applyDescFilters(desc: CustomerCaseDescriptor, filters: FilterBy[]): boolean {
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

export function applySearchString(desc: CustomerCaseDescriptor, searchString: string): boolean {
  const firstName: boolean = desc.firstName?.toLowerCase().indexOf(searchString) > -1;
  return desc.firstName.toLowerCase().indexOf(searchString) > -1 || firstName;
}

export function applyDescFilter(desc: CustomerCaseDescriptor, filter: FilterBy): boolean {
  switch (filter.type) {
    case 'FilterByAssignees': {
      for (const assignee of filter.assignees) {
        if (desc.task.assigneeIds.length === 0 && assignee === _nobody_) {
          continue;
        }
        if (!desc.task.assigneeIds.includes(assignee)) {
          return false;
        }
      }
      return true;
    }
    case 'FilterByRoles': {
      for (const role of filter.roles) {
        if (desc.task.roles.length === 0 && role === _nobody_) {
          continue;
        }
        if (!desc.task.roles.includes(role)) {
          return false;
        }
      }
      return true;
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