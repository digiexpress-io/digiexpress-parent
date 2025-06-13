import React from 'react'
import { useLocale } from '@/api-locale';
import { EveliTaskStats } from '@/eveli-task-stats';

export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<EveliTaskStats />)
}
