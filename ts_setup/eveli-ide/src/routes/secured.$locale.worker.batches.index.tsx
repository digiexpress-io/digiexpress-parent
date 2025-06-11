
import { createFileRoute } from '@tanstack/react-router'
import { EveliBatchesTable } from '@/eveli-batches';

export const Route = createFileRoute('/secured/$locale/worker/batches/')({
  component: Component,
})

function Component() {

  return (<EveliBatchesTable />)
}
