import { createFileRoute } from '@tanstack/react-router'
import { FindAllDeliveries } from '../eveli-task-queue';
import { QueueProvider } from '@dxs-ts/eveli-api';


export const Route = createFileRoute('/secured/$locale/worker/queues/deliveries/')({
  component: Component,
})

function Component() {
  return (
  <QueueProvider>
    <FindAllDeliveries />
  </QueueProvider>)
}
