import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/burger'
import { TaskContainer } from '../frontdesk/views/task/TaskContainer';

export const Route = createFileRoute('/secured/$locale/worker/tasks/create/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<TaskContainer />)
}
