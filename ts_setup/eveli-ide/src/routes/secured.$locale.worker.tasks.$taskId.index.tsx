
import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/secured/$locale/worker/tasks/$taskId/')({
  component: Component,
})

function Component() {
  return null;
}
