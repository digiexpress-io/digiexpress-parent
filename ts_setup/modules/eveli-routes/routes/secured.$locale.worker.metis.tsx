import { Outlet, createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/secured/$locale/worker/metis')({
  component: Component,
})

function Component() {
  return (<Outlet />)
}
