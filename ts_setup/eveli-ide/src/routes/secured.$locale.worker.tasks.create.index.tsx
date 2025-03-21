import React from 'react'
import { createFileRoute } from '@tanstack/react-router'

import { useLocale, EveliTaskComposer } from '@/burger'
import { Container } from '@mui/system';

export const Route = createFileRoute('/secured/$locale/worker/tasks/create/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (

  <Container maxWidth='lg'>
    <EveliTaskComposer />
  </Container>

  )
}
