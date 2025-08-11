import { createFileRoute } from '@tanstack/react-router'

import { FeedbackProvider } from '@dxs-ts/eveli-api';
import { FeedbackAllTasks } from '../eveli-task-feedback';


export const Route = createFileRoute('/secured/$locale/worker/feedback/')({
  component: Component,
})

function Component() {
  return (<FeedbackProvider><FeedbackAllTasks /></FeedbackProvider>)
}
