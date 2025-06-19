import { Outlet } from '@tanstack/react-router'
import { EveliTaskTableProvider } from '@/eveli-tasks';
import { FeedbackProvider } from '@/api-feedback';


export const Route = createFileRoute({
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
