
import { createFileRoute } from '@tanstack/react-router'
import { EveliTenantFeatureEnabled } from '@dxs-ts/eveli-api';
import { EveliMetisStatus } from '@dxs-ts/eveli-primitives';

export const Route = createFileRoute('/secured/$locale/worker/metis/')({
  component: Component,
})

function Component() {
  return (
    <EveliTenantFeatureEnabled id='METIS'>
      <EveliMetisStatus />
    </EveliTenantFeatureEnabled>
  )
}
