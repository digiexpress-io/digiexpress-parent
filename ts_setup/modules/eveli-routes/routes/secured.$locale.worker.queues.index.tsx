import { createFileRoute } from '@tanstack/react-router'
import { FindAllQueues } from '../eveli-task-queue';
import { QueueProvider } from '@dxs-ts/eveli-api';

export const Route = createFileRoute('/secured/$locale/worker/queues/')({
  component: Component,
})

function Component() {
  return (<QueueProvider><FindAllQueues /></QueueProvider>)
}
