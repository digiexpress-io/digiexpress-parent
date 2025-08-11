import { createFileRoute } from '@tanstack/react-router'


import { useTenantConfigFeatures } from '@dxs-ts/eveli-api';
import { EveliTasksTable } from '../eveli-tasks-2';
import { EveliTasks } from '../eveli-tasks';


export const Route = createFileRoute('/secured/$locale/worker/tasks/')({
  component: Component,
})

function Component() {
  const { isEnabled } = useTenantConfigFeatures();
  return isEnabled('SMART_TABLES') ? <EveliTasksTable /> : <EveliTasks />;
}
