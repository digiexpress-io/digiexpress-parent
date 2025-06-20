
import { EveliBatchStep } from '@/eveli-batches-step';

export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { stepId } = Route.useParams();
  return (<EveliBatchStep stepId={stepId} />)
}
