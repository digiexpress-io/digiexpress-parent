
import { createFileRoute } from '@tanstack/react-router'
import { EveliBatchesTable } from '@dxs-ts/eveli-primitives';

export const Route = createFileRoute('/secured/$locale/worker/batches/')({
  component: Component,
})

function Component() {

  return (<EveliBatchesTable />)
}
