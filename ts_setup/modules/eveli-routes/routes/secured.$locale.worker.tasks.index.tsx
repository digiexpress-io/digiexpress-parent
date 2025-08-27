import { createFileRoute } from '@tanstack/react-router'


import { useTenantConfigFeatures } from '@dxs-ts/eveli-api';
import { EveliTasksTable as TasksTable_v2 } from '../eveli-tasks-2';
import { TasksTable as TasksTable_v1  } from '@dxs-ts/task-composer-v1';


export const Route = createFileRoute('/secured/$locale/worker/tasks/')({
  component: Component,
})

function Component() {
  const { isEnabled } = useTenantConfigFeatures();
  return isEnabled('SMART_TABLES') ? <TasksTable_v2 /> : <TasksTable_v1 />;
}
