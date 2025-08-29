import React from 'react'

import { createFileRoute } from '@tanstack/react-router'

import { Container } from '@mui/system';
import { TaskComposer  } from '@dxs-ts/task-composer-v1';
import { TaskCreate } from '@dxs-ts/task-composer-v2'
import { Dialog } from '@mui/material';

//<EveliTaskComposer />
export const Route = createFileRoute('/secured/$locale/worker/tasks/create/')({
  component: Component,
})

function Component() {
  return (
    <Container maxWidth='lg'>
      <Dialog open={true}><TaskCreate /></Dialog>
    </Container>
  )
}
