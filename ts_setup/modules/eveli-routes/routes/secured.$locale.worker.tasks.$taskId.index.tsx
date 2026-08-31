import { createFileRoute } from '@tanstack/react-router'
import { Container } from '@mui/material';

import { useTenantConfigFeatures } from '@dxs-ts/eveli-api';


import { TaskDashboardProd } from '@dxs-ts/task-composer-v2';


export const Route = createFileRoute('/secured/$locale/worker/tasks/$taskId/')({
  component: Component,
})

function Component() {
  const { taskId } = Route.useParams();
  const { isEnabled } = useTenantConfigFeatures();

  return (
    <Container>
      <TaskDashboardProd taskId={taskId} />
    </Container>
  )
}
