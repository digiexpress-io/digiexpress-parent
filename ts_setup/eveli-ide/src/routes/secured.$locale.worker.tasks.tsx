import React from 'react'
import { Outlet } from '@tanstack/react-router'
import { useLocale } from '@/api-locale';
import { EveliTaskTableProvider } from '@/eveli-tasks';
import { FeedbackProvider } from '@/api-feedback';


export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (
    <EveliTaskTableProvider>
      <FeedbackProvider>
        <Outlet />
      </FeedbackProvider>
    </EveliTaskTableProvider>)
}
