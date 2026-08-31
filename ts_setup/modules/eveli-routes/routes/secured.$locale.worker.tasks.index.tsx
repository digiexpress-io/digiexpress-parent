import { createFileRoute } from '@tanstack/react-router'
import { TaskTable } from '@dxs-ts/task-composer-v2';


export const Route = createFileRoute('/secured/$locale/worker/tasks/')({
  component: Component,
})

function Component() {
  return <TaskTable />;
}
