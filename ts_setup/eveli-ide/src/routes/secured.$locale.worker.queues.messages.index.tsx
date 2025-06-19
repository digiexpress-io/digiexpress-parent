import React from 'react'
import { FindAllMessages } from '../eveli-task-queue';
import { QueueProvider } from '../api-queue';

export const Route = createFileRoute({
  component: Component,
})

function Component() {
  return (<QueueProvider><FindAllMessages /></QueueProvider>)
}
