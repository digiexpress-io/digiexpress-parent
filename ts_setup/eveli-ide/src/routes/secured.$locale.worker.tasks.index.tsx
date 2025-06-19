import React from 'react'
import { EveliTasks } from '@/eveli-tasks';
import { useTenantConfigFeatures } from '@/api-tenant-config';
import { EveliTasksTable } from '@/eveli-tasks-2';


export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { isEnabled } = useTenantConfigFeatures();
  return isEnabled('SMART_TABLES') ? <EveliTasksTable /> : <EveliTasks />;
}
