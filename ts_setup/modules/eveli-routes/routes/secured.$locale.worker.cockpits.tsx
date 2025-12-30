import { createFileRoute } from '@tanstack/react-router'
import { CockpitTable } from '@dxs-ts/cockpit-composer'

export const Route = createFileRoute('/secured/$locale/worker/cockpits')({
  component: Component,
})

function Component() {
  return (<CockpitTable />)

}