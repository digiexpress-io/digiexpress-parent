import { TaskApi } from "@/api-task";
import { Row } from "@tanstack/react-table";


const priorityOrder: Record<TaskApi.TaskPriority, number> = {
  LOW: 0,
  NORMAL: 1,
  HIGH: 2,
};

const statusOrder: Record<TaskApi.TaskStatus, number> = {
  NEW: 0,
  OPEN: 1,
  COMPLETED: 2,
  REJECTED: 3
}

export function taskSortingFn(rowA: Row<TaskApi.Task>, rowB: Row<TaskApi.Task>, columnId: string) {
  const a = rowA.original[columnId as keyof TaskApi.Task];
  const b = rowB.original[columnId as keyof TaskApi.Task];

  console.log("columnId", columnId)

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

      console.log('assignedUser1', assignedUser1)

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
