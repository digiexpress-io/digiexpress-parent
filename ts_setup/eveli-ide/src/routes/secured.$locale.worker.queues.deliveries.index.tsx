import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/api-locale'
import { FindAllDeliveries } from '../eveli-task-queue';
import { QueueProvider } from '../api-queue';


export const Route = createFileRoute('/secured/$locale/worker/queues/deliveries/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (
  <QueueProvider>
    <FindAllDeliveries />
  </QueueProvider>)
}
