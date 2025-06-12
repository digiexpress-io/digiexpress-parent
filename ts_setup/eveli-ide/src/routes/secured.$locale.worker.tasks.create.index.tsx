import React from 'react'

import { Container } from '@mui/system';
import { useLocale } from '@/api-locale';
import { EveliTaskComposer } from '@/eveli-task-composer';

export const Route = createFileRoute({
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
