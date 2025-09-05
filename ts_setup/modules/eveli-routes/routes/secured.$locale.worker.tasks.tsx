import { Outlet, createFileRoute } from '@tanstack/react-router'
import { AnyTaskRoute } from '../eveli-any-task-route';


export const Route = createFileRoute('/secured/$locale/worker/tasks')({
  component: Component,
})

function Component() {
  return (<AnyTaskRoute><Outlet /></AnyTaskRoute>)
}

