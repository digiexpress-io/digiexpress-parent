import { createFileRoute } from '@tanstack/react-router'
import { Container, useTheme } from '@mui/material';

import { useTenantConfigFeatures } from '@dxs-ts/eveli-api';

import { EveliTaskComposer } from '../eveli-task-composer';
import { EveliTaskDashboard } from '../eveli-task-composer-v2';
import { EveliTaskDashboardProd } from '../eveli-task-composer-v2/EveliTaskDashboardProd';


export const Route = createFileRoute('/secured/$locale/worker/tasks/$taskId/')({
  component: Component,
})

function Component() {
  const { taskId } = Route.useParams();
  const { isEnabled } = useTenantConfigFeatures();
  const theme = useTheme();
  const isNew = isEnabled('SMART_TASK');

  return (
    <Container>
      {isNew ? <EveliTaskDashboardProd taskId={taskId} /> : <EveliTaskComposer taskId={taskId} />}
    </Container>
  )
}
