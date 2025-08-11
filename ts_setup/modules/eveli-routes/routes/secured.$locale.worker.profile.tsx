import { createFileRoute } from '@tanstack/react-router'
import { UserProfile } from '../eveli-user-profile';


export const Route = createFileRoute('/secured/$locale/worker/profile')({
  component: Component,
})

function Component() {
  return (<UserProfile />)
}
