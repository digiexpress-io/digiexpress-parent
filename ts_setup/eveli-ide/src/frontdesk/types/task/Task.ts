import { TaskLink } from "./TaskLink";

export enum TaskPriority {
  LOW = 'LOW',
  NORMAL = 'NORMAL',
  HIGH = 'HIGH',
}

export enum TaskStatus {
  NEW = 'NEW',
  OPEN = 'OPEN',
  COMPLETED = 'COMPLETED',
  REJECTED = 'REJECTED'
}

export interface Task {
  id?: number;
  taskRef?: string;
  version?: number;
  created?: Date;
  updated?: Date;
  completed?: Date;
  assignedUser?: string;
  assignedUserEmail?: string;
  updaterId?: string;
  dueDate?: Date;
  status?: TaskStatus;
  subject?: string;
  description?: string;
  priority?: TaskPriority;
  keyWords?: string[];
  taskLinks?: TaskLink[];
  // For UI purposes
  statusCode?: number;
  priorityCode?: number;
  clientIdentificator?: string|null;
  assignedRoles?: string[]|null;
}
