
import { createFileRoute } from '@tanstack/react-router'
import { EveliBatchView } from '@dxs-ts/eveli-primitives';

export const Route = createFileRoute('/secured/$locale/worker/batches/$batchId/')({
  component: Component,
})

function Component() {
  const { batchId } = Route.useParams();
  return (<EveliBatchView batchId={batchId} />)
}
