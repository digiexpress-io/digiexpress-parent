
import { createFileRoute } from '@tanstack/react-router'


export const Route = createFileRoute('/secured/$locale/worker/batches/create/')({
  component: Component,
})

function Component() {

  return (<>...create</>)
}
