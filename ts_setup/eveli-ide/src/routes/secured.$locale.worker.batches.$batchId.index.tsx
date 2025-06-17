
import { EveliBatchView } from '@/eveli-batches';

export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { batchId } = Route.useParams();
  return (<EveliBatchView batchId={batchId} />)
}
