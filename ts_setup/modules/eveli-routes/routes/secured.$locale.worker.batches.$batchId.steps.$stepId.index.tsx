
import { createFileRoute } from '@tanstack/react-router'
import { EveliBatchStep } from '@dxs-ts/eveli-primitives';

export const Route = createFileRoute('/secured/$locale/worker/batches/$batchId/steps/$stepId/')({
  component: Component,
})

function Component() {
  const { stepId } = Route.useParams();
  return (<EveliBatchStep stepId={stepId} />)
}
