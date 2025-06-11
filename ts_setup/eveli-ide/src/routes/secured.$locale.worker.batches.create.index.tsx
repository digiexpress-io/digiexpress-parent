
import { createFileRoute } from '@tanstack/react-router'
import { EveliBatchesTable } from '@/eveli-batches';

export const Route = createFileRoute('/secured/$locale/worker/batches/create/')({
  component: Component,
})

function Component() {

  return (<>...create</>)
}
