import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/burger'
import { FindAllQueues, QueueProvider } from '../queue';

export const Route = createFileRoute('/secured/$locale/worker/queues/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<QueueProvider><FindAllQueues /></QueueProvider>)
}
