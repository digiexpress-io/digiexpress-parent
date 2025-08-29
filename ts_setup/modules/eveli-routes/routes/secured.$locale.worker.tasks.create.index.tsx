import { createFileRoute } from '@tanstack/react-router'

import { Container } from '@mui/system';
import { TaskComposer  } from '@dxs-ts/task-composer-v1';
import { TaskCreate } from '@dxs-ts/task-composer-v2'
import { useTenantConfigFeatures } from '@dxs-ts/eveli-api';


export const Route = createFileRoute('/secured/$locale/worker/tasks/create/')({
  component: Component,
})

function Component() {
  const { isEnabled } = useTenantConfigFeatures();
  const isNew = isEnabled('SMART_TASK');


  return (
    <Container maxWidth='lg'>
      {isNew ? <TaskCreate /> : <TaskComposer />}
    </Container>
  )
}
