import { createFileRoute } from '@tanstack/react-router'
import { Container } from '@mui/material';

import { useTenantConfigFeatures } from '@dxs-ts/eveli-api';

import { TaskComposer } from '@dxs-ts/task-composer-v1';
import { TaskDashboard } from '@dxs-ts/task-composer-v2';
import { TaskDashboardProd } from '@dxs-ts/task-composer-v2';


export const Route = createFileRoute('/secured/$locale/worker/tasks/$taskId/')({
  component: Component,
})

function Component() {
  const { taskId } = Route.useParams();
  const { isEnabled } = useTenantConfigFeatures();
  const isNew = isEnabled('SMART_TASK');

  return (
    <Container>
      {isNew ? <TaskDashboardProd taskId={taskId} /> : <TaskComposer taskId={taskId} />}
    </Container>
  )
}
