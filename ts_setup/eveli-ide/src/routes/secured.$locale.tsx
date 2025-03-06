import { Outlet, createFileRoute } from '@tanstack/react-router'


export const Route = createFileRoute('/secured/$locale')({
  component: Component,
})

function Component() {
  return <Outlet/>
}
