import { Outlet, createFileRoute } from '@tanstack/react-router'

import { FeedbackProvider } from '@dxs-ts/eveli-api';
import { EveliTaskTableProvider } from '../eveli-tasks';


export const Route = createFileRoute('/secured/$locale/worker/tasks')({
  component: Component,
})

function Component() {
  return (
    <EveliTaskTableProvider>
      <FeedbackProvider>
        <Outlet />
      </FeedbackProvider>
    </EveliTaskTableProvider>)
}
