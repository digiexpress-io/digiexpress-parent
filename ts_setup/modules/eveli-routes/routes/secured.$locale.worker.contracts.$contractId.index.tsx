import { createFileRoute } from '@tanstack/react-router'

import { ContractDashboard } from '@dxs-ts/contract-composer';
import { Container } from '@mui/material';

export const Route = createFileRoute('/secured/$locale/worker/contracts/$contractId/')({
  component: Component,
})

function Component() {
  return (
    <Container>
      <ContractDashboard />
    </Container>
  )


}