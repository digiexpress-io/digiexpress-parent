import React from 'react'
import { createFileRoute, Outlet } from '@tanstack/react-router'
import { useLocale } from '@/burger'
import { TaskSessionContext } from '../frontdesk/context/TaskSessionContext';
import { FeedbackProvider } from '../feedback';

export const Route = createFileRoute('/secured/$locale/worker/tasks')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (
    <TaskSessionContext>
      <FeedbackProvider>
        <Outlet />
      </FeedbackProvider>
    </TaskSessionContext>)
}
