import React from 'react'
import { useLocale } from '@/api-locale'
import { FindAllQueues } from '../eveli-task-queue';
import { QueueProvider } from '../api-queue';

export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<QueueProvider><FindAllQueues /></QueueProvider>)
}
