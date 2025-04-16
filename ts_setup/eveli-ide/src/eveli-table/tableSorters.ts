import { TaskApi } from "@/api-task";
import { Row } from "@tanstack/react-table";


const priorityOrder: Record<TaskApi.TaskPriority, number> = {
  LOW: 0,
  NORMAL: 1,
  HIGH: 2,
};

export function prioritySortingFn(rowA: Row<TaskApi.Task>, rowB: Row<TaskApi.Task>, columnId: string) {
  const aRaw = rowA.original[columnId as keyof TaskApi.Task];
  const bRaw = rowB.original[columnId as keyof TaskApi.Task];

  const a = priorityOrder[aRaw as TaskApi.TaskPriority] ?? -1;
  const b = priorityOrder[bRaw as TaskApi.TaskPriority] ?? -1;

  if (a > b) {
    return 1;
  } else if (a < b) {
    return -1;
  } else {
    return 0;
  }
}

const statusOrder: Record<TaskApi.TaskStatus, number> = {
  NEW: 0,
  OPEN: 1,
  COMPLETED: 2,
  REJECTED: 3
}

export function statusSortingFn(rowA: Row<TaskApi.Task>, rowB: Row<TaskApi.Task>, columnId: string) {
  const aRaw = rowA.original[columnId as keyof TaskApi.Task];
  const bRaw = rowB.original[columnId as keyof TaskApi.Task];

  const a = statusOrder[aRaw as TaskApi.TaskStatus] ?? -1;
  const b = statusOrder[bRaw as TaskApi.TaskStatus] ?? -1;

  if (a > b) {
    return 1;
  } else if (a < b) {
    return -1;
  } else {
    return 0;
  }

}