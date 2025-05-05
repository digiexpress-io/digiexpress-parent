import { useIntl } from 'react-intl';

import { TaskApi } from '@/api-task';
import { TaskPriorityStatistics } from './stat-types';


type PriorityTitleMap = {
  [priority in TaskApi.TaskPriority]: string
}

const taskPriorityMapping: PriorityTitleMap = {
  'LOW': 'task.priority.low',
  'NORMAL': 'task.priority.normal',
  'HIGH': 'task.priority.high',
}

export function usePriorityCount(dashboard: TaskApi.TaskDasboard): { 
  taskPriorityNames: { priority: string, count: number }[] | undefined;
  taskPriorityStats: TaskPriorityStatistics[] | undefined;
  taskPriorityMapping: PriorityTitleMap;
} {
  const intl = useIntl();
  const taskPriorityStats: TaskPriorityStatistics[] = dashboard.events
    .filter(({ eventType }) => eventType === 'PRIORITY')
    .map(event => ({
      count: event.eventCount,
      priority: event.attributeValue as TaskApi.TaskPriority
    }));

  return { 
    taskPriorityStats,
    taskPriorityNames: taskPriorityStats.map(stats => ({
      priority: intl.formatMessage({ id: taskPriorityMapping[stats.priority] }),
      count: stats.count
    })),
    taskPriorityMapping }
}