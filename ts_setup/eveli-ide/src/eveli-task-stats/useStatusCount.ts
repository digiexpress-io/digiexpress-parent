import { useIntl } from 'react-intl';
import { TaskApi } from '@/api-task';
import { TaskStatusStatistics } from './stat-types';


type StatusTitleMap = {
  [status in TaskApi.TaskStatus]: string
}
const taskStatusMapping: StatusTitleMap = {
  'NEW': 'task.status.new',
  'OPEN': 'task.status.open',
  'REJECTED': 'task.status.rejected',
  'COMPLETED': 'task.status.completed',
  'TRANSFERRED': 'task.status.transferred',
}

export function useStatusCount(dashboard: TaskApi.TaskDasboard): { 
  taskStatusNames: { status: string, count: number }[] | undefined,
  taskStatusStats: TaskStatusStatistics[] | undefined,
  taskStatusMapping: StatusTitleMap
} {

  const intl = useIntl();  
  const taskStatusStats: TaskStatusStatistics[] = dashboard.events
  .filter(({ eventType }) => eventType === 'STATUS')
  .map(event => ({
    count: event.eventCount,
    status: event.attributeValue as TaskApi.TaskStatus
  }));

  return { 
    taskStatusStats,
    taskStatusMapping,
    taskStatusNames: taskStatusStats.map(stats => ({
      status: intl.formatMessage({ id: taskStatusMapping[stats.status] }),
      count: stats.count
    })),
  }
}