import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/secured/$locale/worker/cockpits/$cockpitId/')({
  component: Component,
})

function Component() {
  const { cockpitId } = Route.useParams();

  return (
    <div>Edit Cockpit {cockpitId}</div>
  )
}