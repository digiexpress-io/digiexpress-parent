import { createFileRoute, Outlet } from '@tanstack/react-router'
import { AnyLedgerRoute } from '../eveli-any-ledger-route'


export const Route = createFileRoute('/secured/$locale/worker/ledgers')({
  component: Component
})

function Component() {
  return (<AnyLedgerRoute><Outlet /></AnyLedgerRoute>)
}