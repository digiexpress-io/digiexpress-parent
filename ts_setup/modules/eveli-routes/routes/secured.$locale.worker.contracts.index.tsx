import { ContractTable } from '@dxs-ts/contract-composer'
import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/secured/$locale/worker/contracts/')({
  component: Component,
})

function Component() {
  return (<ContractTable />)
}