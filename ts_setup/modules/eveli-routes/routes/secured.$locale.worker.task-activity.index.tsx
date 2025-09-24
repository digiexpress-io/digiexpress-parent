import { createFileRoute } from '@tanstack/react-router'
import { EveliTaskActivity } from '../eveli-task-activity';



export const Route = createFileRoute('/secured/$locale/worker/task-activity/')({
  component: Component,
})

function Component() {
  return (<EveliTaskActivity />)
}
