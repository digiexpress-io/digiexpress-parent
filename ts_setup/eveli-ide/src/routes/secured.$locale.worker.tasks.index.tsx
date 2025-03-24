import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/api-locale';
import { EveliTasks } from '@/eveli-tasks';


export const Route = createFileRoute('/secured/$locale/worker/tasks/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<EveliTasks />)
}
