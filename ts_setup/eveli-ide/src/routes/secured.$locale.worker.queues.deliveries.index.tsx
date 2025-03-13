import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/burger'
import { FindAllDeliveries, QueueProvider } from '../queue';


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
