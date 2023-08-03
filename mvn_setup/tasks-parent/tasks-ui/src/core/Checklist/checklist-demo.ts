import type { Checklist, ChecklistItem } from "taskclient/task-types";

export const demoAssignees: string[] = ['John Doe', 'Jane Doe', 'John Smith', 'Jane Smith'];

const demoChecklistItems: ChecklistItem[] = [
  {
    id: 'CHECK_1',
    title: 'Item 1',
    completed: true,
    dueDate: '07/25/2023',
    assigneeIds: [],
  },
  {
    id: 'CHECK_2',
    title: 'Item 2',
    completed: false,
    dueDate: '07/25/2023',
    assigneeIds: ['John Doe', 'Jane Doe'],
  },
  {
    id: 'CHECK_3',
    title: 'Item 3',
    completed: false,
    dueDate: undefined,
    assigneeIds: [],
  },
];

export const demoChecklist: Checklist = {
  id: 'CHECKLIST_1',
  title: 'Checklist 1',
  items: demoChecklistItems,
};
