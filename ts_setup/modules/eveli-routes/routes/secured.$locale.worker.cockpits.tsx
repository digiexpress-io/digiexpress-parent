import { Outlet, createFileRoute } from '@tanstack/react-router'
import { AnyCockpitRoute } from '../eveli-any-cockpit-route'

export const Route = createFileRoute('/secured/$locale/worker/cockpits')({
  component: Component,
})

function Component() {
  return (
    <AnyCockpitRoute><Outlet /></AnyCockpitRoute>
  )
}