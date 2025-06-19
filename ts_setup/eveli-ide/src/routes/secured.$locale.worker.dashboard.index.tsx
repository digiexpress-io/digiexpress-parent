import { EveliTaskStats } from '@/eveli-task-stats';

export const Route = createFileRoute({
  component: Component,
})

function Component() {
  return (<EveliTaskStats />)
}
