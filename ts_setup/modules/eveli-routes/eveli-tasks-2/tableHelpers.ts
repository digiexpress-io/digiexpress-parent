import { TaskApi } from '@dxs-ts/task-api';
import { FilterFnOption, Row } from "@tanstack/react-table";
import { DateTime } from "luxon";


const priorityOrder: Record<TaskApi.TaskPriority, number> = {
  LOW: 0,
  NORMAL: 1,
  HIGH: 2,
};

const statusOrder: Record<TaskApi.TaskStatus, number> = {
  NEW: 0,
  OPEN: 1,
  COMPLETED: 2,
  REJECTED: 3,
  TRANSFERRED: 4,
  DELEGATED: 5,
  WAITING: 6
}

export function taskSortingFn(rowA: Row<TaskApi.Task>, rowB: Row<TaskApi.Task>, columnId: string) {
  const a = rowA.original[columnId as keyof TaskApi.Task];
  const b = rowB.original[columnId as keyof TaskApi.Task];

  switch (columnId) {
    case 'priority': {
      const aVal = priorityOrder[a as TaskApi.TaskPriority] ?? -1;
      const bVal = priorityOrder[b as TaskApi.TaskPriority] ?? -1;
      return aVal - bVal;
    }
    case 'status': {
      const aVal = statusOrder[a as TaskApi.TaskStatus] ?? -1;
      const bVal = statusOrder[b as TaskApi.TaskStatus] ?? -1;
      return aVal - bVal;
    }
    case 'subject': {
      const taskName1 = a?.toString() ?? '';
      const taskName2 = b?.toString() ?? '';
      return taskName1.localeCompare(taskName2);
    }
    case 'assignedUser': {
      const assignedUser1 = a?.toString().trim() ?? '';
      const assignedUser2 = b?.toString().trim() ?? '';
      return assignedUser1.localeCompare(assignedUser2);
    }
    default: {
      if (typeof a === 'string' && typeof b === 'string') {
        return a.localeCompare(b);
      }
      return 0;
    }
  }
}

export const filterTaskRefOrSubjectFn: FilterFnOption<TaskApi.Task> = (row, _columnId: string, filterValue: string[]) => {
  const subject = row.original.subject?.toLowerCase() || '';
  const taskRef = row.original.taskRef?.toLowerCase() || '';
  const cleanedFilterValues = Array.isArray(filterValue) ? filterValue.map((filter) => filter.toLowerCase()) : [(filterValue as string).toLowerCase()];

  if (!filterValue || filterValue.length === 0) {
    return true;
  }

  return cleanedFilterValues.some((filter) => {
    return subject.includes(filter) || taskRef.includes(filter);
  })
}

export const filterFormattedDateFn: FilterFnOption<TaskApi.Task> = (row, columnId, filterValue) => {
  const rawDate = row.getValue(columnId) as string | undefined;

  if (!rawDate) {
    return false;
  }

  const formatted = DateTime.fromISO(rawDate).toFormat('d.M.yyyy').toLowerCase();
  const filters = Array.isArray(filterValue) ? filterValue : [filterValue];

  return filters.some(f => formatted.includes(f.toLowerCase()));
};



function normalize(input: string | string[]): string[] {
  const normalized: string[] = [];
  if (Array.isArray(input)) {
    normalized.push(...input);
  } else if (input) {
    normalized.push(input);
  }

  return normalized
    .filter(value => !!value?.trim())
    .map(value => value.toLowerCase())
}

export const filterStringOrArrayFn: FilterFnOption<TaskApi.Task> = (row, columnId: string, initFilters: string | string[]) => {
  const filters = normalize(initFilters);
  if (filters.length === 0) {
    return true;
  }
  const rawValue: string | string[] | undefined | null = row.getValue(columnId);
  if (rawValue === null || rawValue === undefined) {
    return false;
  }
  const valueToFilter = normalize(rawValue);
  return filters.some((filter) => valueToFilter.some((target) => target.includes(filter))
  );
}

