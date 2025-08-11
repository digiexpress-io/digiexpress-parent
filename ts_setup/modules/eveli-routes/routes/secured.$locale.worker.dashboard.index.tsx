import { createFileRoute } from '@tanstack/react-router'
import { EveliTaskStats } from '../eveli-task-stats';

export const Route = createFileRoute('/secured/$locale/worker/dashboard/')({
  component: Component,
})

function Component() {
  return (<EveliTaskStats />)
}
