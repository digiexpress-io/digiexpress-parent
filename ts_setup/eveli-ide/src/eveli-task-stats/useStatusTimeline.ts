import { TaskApi } from '@/api-task';
import { TaskStatusTimelineStatistics } from './stat-types';


export function useStatusTimeline(dashboard: TaskApi.TaskDasboard): { taskTimelineStats: TaskStatusTimelineStatistics[] | undefined } {

  const data = dashboard.events
  .filter(({ eventType }) => eventType === 'STATUS_DATE')
  .reduce<Record<string, TaskStatusTimelineStatistics>>((collector, current) => {

    if(!collector[current.eventDate]) {
      collector[current.eventDate] = {
        statusDate: current.eventDate as any,
        completed: 0,
        new: 0,
        open: 0,
        rejected: 0
      };
    }

    const target: TaskStatusTimelineStatistics = collector[current.eventDate];
    const key: 'new' | 'open' | 'completed' | 'rejected'  = current.attributeValue.toLowerCase() as any;
    target[key] = (target[key] ?? 0) + current.eventCount;

    return collector;
  }, {});

  const taskTimelineStats: TaskStatusTimelineStatistics[] = Object.values(data).sort((a,b)=>(new Date(a.statusDate).getTime() - new Date(b.statusDate).getTime()));
  return { taskTimelineStats }
}