import { createFileRoute } from '@tanstack/react-router'
import React from 'react'
import { FindAllMessages } from '../eveli-task-queue';
import { QueueProvider } from '@dxs-ts/eveli-api';

export const Route = createFileRoute('/secured/$locale/worker/queues/messages/')({
  component: Component,
})

function Component() {
  return (<QueueProvider><FindAllMessages /></QueueProvider>)
}
