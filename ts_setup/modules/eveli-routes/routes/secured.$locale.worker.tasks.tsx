import { Outlet, createFileRoute } from '@tanstack/react-router'

import { FeedbackProvider } from '@dxs-ts/task-feedback';
import { useFeedbackBackend } from '@dxs-ts/eveli-api';
import { EveliTaskTableProvider } from '../eveli-tasks';


export const Route = createFileRoute('/secured/$locale/worker/tasks')({
  component: Component,
})

function Component() {
  const backend = useFeedbackBackend();
  return (
    <EveliTaskTableProvider>
      <FeedbackProvider backend={backend}>
        <Outlet />
      </FeedbackProvider>
    </EveliTaskTableProvider>)
}
