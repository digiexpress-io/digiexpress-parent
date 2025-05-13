import { TaskApi } from "@/api-task";
import { FilterFnOption, Row } from "@tanstack/react-table";


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
  TRANSFERRED: 4
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


export const filterStringOrArrayFn: FilterFnOption<TaskApi.Task> = (row, columnId: string, filterValue: string | string[]) => {
  const rowValue = row.getValue(columnId) as string;
  const target = Array.isArray(rowValue) ? rowValue.map((v) => v.toLowerCase()) : [(rowValue as string).toLowerCase()];

  if (!filterValue || filterValue.length === 0) {
    return true;
  }

  const filters = Array.isArray(filterValue) ? filterValue.map((f) => f.toLowerCase()) : [filterValue.toLowerCase()];

  return filters.some((filter) => target.some((target) => target.includes(filter))
  );
}

/*
export const filterStringOrArrayFn: FilterFnOption<TaskApi.Task> = (row, columnId: string, filterValue: string | string[]) => {
  const rowValue = row.getValue(columnId) as string;
  const target = rowValue?.toLowerCase();

  const filters = Array.isArray(filterValue) ? filterValue.map((f) => f.toLowerCase()) : [filterValue.toLowerCase()];

  if (!filterValue || filterValue.length === 0) {
    return true;
  }

  return filters.some((filter) => target?.includes(filter));
}
*/

