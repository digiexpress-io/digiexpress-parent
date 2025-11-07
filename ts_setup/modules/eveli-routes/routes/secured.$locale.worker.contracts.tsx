import { createFileRoute, Outlet } from '@tanstack/react-router'
import { AnyContractRoute } from '../eveli-any-contract-route'


export const Route = createFileRoute('/secured/$locale/worker/contracts')({
  component: Component
})

function Component() {
  return (<AnyContractRoute><Outlet /></AnyContractRoute>)
}