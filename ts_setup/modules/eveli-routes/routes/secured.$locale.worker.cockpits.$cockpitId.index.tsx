import { createFileRoute } from '@tanstack/react-router'
import { Container } from '@mui/material';

import { CockpitDashboard } from '@dxs-ts/cockpit-composer';

export const Route = createFileRoute('/secured/$locale/worker/cockpits/$cockpitId/')({
  component: Component,
})

function Component() {
  const { cockpitId } = Route.useParams();

  return (
    <Container>
      <CockpitDashboard cockpitId={cockpitId} />
    </Container>
  )
}