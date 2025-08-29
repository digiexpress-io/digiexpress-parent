import { createFileRoute } from '@tanstack/react-router'
import React from 'react'

import { Container } from '@mui/system';
import { TaskComposer  } from '@dxs-ts/task-composer-v1';


//<EveliTaskComposer />
export const Route = createFileRoute('/secured/$locale/worker/tasks/create/')({
  component: Component,
})

function Component() {
  return (
    <Container maxWidth='lg'>
      <TaskComposer />
    </Container>
  )
}
