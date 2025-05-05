import { TaskApi } from '@/api-task';
import { OverdueByGroupStatistics } from './stat-types';



export function useTaskOverdue(dashboard: TaskApi.TaskDasboard): { 
  tasksOverdue: { assignedId: string, count: number }[] | undefined
} {

  const tasksOverdue: OverdueByGroupStatistics[] = dashboard.events
  .filter(({ eventType }) => eventType === 'OVERDUE')
  .map(event => ({
    assignedId: event.attributeValue,
    count: event.eventCount
  }));

  return { tasksOverdue }
}