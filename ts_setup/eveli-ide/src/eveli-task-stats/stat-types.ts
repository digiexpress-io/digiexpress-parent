import { TaskApi } from "@/api-task"





export type TaskPriorityStatistics = {
  count: number
  priority: TaskApi.TaskPriority
}

export type TaskStatusStatistics = {
  count: number
  status: TaskApi.TaskStatus
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