import { createFileRoute } from '@tanstack/react-router'
import React from 'react'

import { Container } from '@mui/system';
import { EveliTaskComposer } from '../eveli-task-composer';

export const Route = createFileRoute('/secured/$locale/worker/tasks/create/')({
  component: Component,
})

function Component() {
  return (
    <Container maxWidth='lg'>
      <EveliTaskComposer />
    </Container>
  )
}
