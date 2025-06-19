import { FindAllDeliveries } from '../eveli-task-queue';
import { QueueProvider } from '../api-queue';


export const Route = createFileRoute({
  component: Component,
})

function Component() {
  return (
  <QueueProvider>
    <FindAllDeliveries />
  </QueueProvider>)
}
