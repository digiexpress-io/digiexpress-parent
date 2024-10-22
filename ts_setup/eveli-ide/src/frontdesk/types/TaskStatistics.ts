import { TaskPriority, TaskStatus } from "./task/Task"

export type TaskPriorityStatistics = {
  count: number
  priority: TaskPriority
}

export type TaskStatusStatistics = {
  count: number
  status: TaskStatus
}

export type OverdueByGroupStatistics = {
  count: number
  assignedId: string
}

export type TaskStatusTimelineStatistics = {
  statusDate: Date
  new: number
  open: number
  completed: number
  rejected: number
}