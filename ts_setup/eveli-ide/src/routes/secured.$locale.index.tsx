import { useTenantConfig } from '@/api-tenant-config';
import { Navigate } from '@tanstack/react-router'


export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { features } = useTenantConfig();

  if(features.includes('wrench-only')) {
    return <Navigate {...{
      from: '/secured/$locale',
      to: '/secured/$locale/assets/wrench',
      search: {
        explorer: [],
        explorerActive: undefined
      }
    }}/>
  }

  return <Navigate {...{
    from: '/secured/$locale',
    to: '/secured/$locale/worker/tasks'
  }}/>
}
