import { Outlet, createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/secured/$locale/views/$viewId')({
  component: Component,
})

function Component() {
  return <Outlet />
}


