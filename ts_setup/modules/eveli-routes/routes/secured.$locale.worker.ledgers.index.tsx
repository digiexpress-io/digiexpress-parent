import { LedgerTable } from '@dxs-ts/ledger-composer'
import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/secured/$locale/worker/ledgers/')({
  component: Component,
})

function Component() {
  return (<LedgerTable />)
}