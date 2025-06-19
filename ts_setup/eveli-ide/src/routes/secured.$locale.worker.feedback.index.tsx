import { FeedbackAllTasks } from '@/eveli-task-feedback';
import { FeedbackProvider } from '@/api-feedback';


export const Route = createFileRoute({
  component: Component,
})

function Component() {
  return (<FeedbackProvider><FeedbackAllTasks /></FeedbackProvider>)
}
