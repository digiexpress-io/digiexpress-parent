import { Navigate, createFileRoute } from '@tanstack/react-router'


export const Route = createFileRoute('/secured/$locale/')({
  component: Component,
})

function Component() {
  return <Navigate {...{
    from: '/secured/$locale',
    to: '/secured/$locale/worker/tasks'
  }}/>
}
