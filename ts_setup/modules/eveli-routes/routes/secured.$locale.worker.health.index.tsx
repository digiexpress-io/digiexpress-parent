import { createFileRoute } from '@tanstack/react-router'
import { EveliHealth } from '../eveli-health';



export const Route = createFileRoute('/secured/$locale/worker/health/')({
  component: Component,
})

function Component() {
  return (<EveliHealth />)
}
