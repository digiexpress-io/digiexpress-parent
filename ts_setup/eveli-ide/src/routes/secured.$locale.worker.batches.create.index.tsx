
import { createFileRoute } from '@tanstack/react-router'
import { EveliBatchesTable } from '@/eveli-batches';

export const Route = createFileRoute('/secured/$locale/worker/baches/create/')({
  component: Component,
})

function Component() {

  return (<>...create</>)
}
