import React from 'react'
import { createFileRoute } from '@tanstack/react-router'

import { Container } from '@mui/material';
import { useLocale } from '@/api-locale';
import { EveliTaskComposer } from '@/eveli-task-composer';

export const Route = createFileRoute('/secured/$locale/worker/tasks/$taskId/')({
  component: Component,
})

function Component() {
  const { locale, taskId } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (
    <Container maxWidth='lg'>
      <EveliTaskComposer taskId={taskId}  />
    </Container>
  )
}
