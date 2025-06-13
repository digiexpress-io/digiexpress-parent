import React from 'react'
import { useLocale } from '@/api-locale';
import { EveliTasks } from '@/eveli-tasks';
import { useTenantConfigFeatures } from '@/api-tenant-config';
import { EveliTasksTable } from '@/eveli-tasks-2';


export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();
  const { isEnabled } = useTenantConfigFeatures();
  
  React.useLayoutEffect(() => setLocale(locale), [locale])

  return isEnabled('SMART_TABLES') ? <EveliTasksTable /> : <EveliTasks />;
}
