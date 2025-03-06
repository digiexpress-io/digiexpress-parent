import { createFileRoute, Outlet } from '@tanstack/react-router'


export const Route = createFileRoute('/secured/$locale/assets')({
  component: Component,
})

function Component() {

  return (<Outlet />)
}
