import React from 'react'
import { useLocale } from '@/api-locale'
import { FeedbackAllTasks } from '@/eveli-task-feedback';
import { FeedbackProvider } from '@/api-feedback';


export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<FeedbackProvider><FeedbackAllTasks /></FeedbackProvider>)
}
