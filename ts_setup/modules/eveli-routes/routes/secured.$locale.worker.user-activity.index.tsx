import { createFileRoute } from '@tanstack/react-router'
import { EveliUserActivity } from '../eveli-user-activity'



export const Route = createFileRoute('/secured/$locale/worker/user-activity/')({
  component: Component,
})

function Component() {
  return (<EveliUserActivity />)
}
