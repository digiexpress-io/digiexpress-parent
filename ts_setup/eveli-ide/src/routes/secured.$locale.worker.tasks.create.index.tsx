import React from 'react'

import { Container } from '@mui/system';
import { EveliTaskComposer } from '@/eveli-task-composer';

export const Route = createFileRoute({
  component: Component,
})

function Component() {
  return (
    <Container maxWidth='lg'>
      <EveliTaskComposer />
    </Container>
  )
}
