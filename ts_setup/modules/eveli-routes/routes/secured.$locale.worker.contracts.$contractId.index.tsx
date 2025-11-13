import { createFileRoute } from '@tanstack/react-router'
import { ContractProvider } from '@dxs-ts/contract-api';
import { ContractDashboard } from '@dxs-ts/contract-composer';
import { Container } from '@mui/material';

export const Route = createFileRoute('/secured/$locale/worker/contracts/$contractId/')({
  component: Component,
})

function Component() {
  const { contractId } = Route.useParams();
  return (
    <ContractProvider contractId={contractId}>
      <Container>
        <ContractDashboard />
      </Container>
    </ContractProvider>
  )


}