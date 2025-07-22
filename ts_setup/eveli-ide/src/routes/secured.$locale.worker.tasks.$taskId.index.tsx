import { Container } from '@mui/material';
import { EveliTaskComposer } from '@/eveli-task-composer';
import { EveliTaskDashboard } from '@/eveli-task-composer-v2';

import { useTenantConfigFeatures } from '@/api-tenant-config';

export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { taskId } = Route.useParams();
  const { isEnabled } = useTenantConfigFeatures();

  const isNew = isEnabled('SMART_TASK');

  return (
    <Container maxWidth='lg'>
      {isNew ? <EveliTaskDashboard taskId={taskId} /> : <EveliTaskComposer taskId={taskId} />}
    </Container>
  )
}
