import { createFileRoute } from '@tanstack/react-router'
import { LedgerProvider } from '@dxs-ts/ledger-api';
import { LedgerDashboard } from '@dxs-ts/ledger-composer';
import { Container } from '@mui/material';

export const Route = createFileRoute('/secured/$locale/worker/ledgers/$ledgerId/')({
  component: Component,
})

function Component() {
  const { ledgerId } = Route.useParams();
  return (
    <LedgerProvider ledgerId={ledgerId}>
      <Container>
        <LedgerDashboard />
      </Container>
    </LedgerProvider>
  )


}