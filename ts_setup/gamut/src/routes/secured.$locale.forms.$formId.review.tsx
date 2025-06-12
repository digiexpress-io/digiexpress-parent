import { createFileRoute } from '@tanstack/react-router'
import { Container } from '@mui/material'

import { GShell } from '../g-shell'
import { GFormTip } from '../g-form'
import { WithFormProvider } from '../api-dialob'

export const Route = createFileRoute('/secured/$locale/forms/$formId/review')({
  component: Component,
})
function Component() {
  const { formId } = Route.useParams();

  function handleOnComplete() {

  }
  
  return (<>
    <GShell drawerOpen={false}>
      <main role='main'>
        <Container>
          <WithFormProvider id={formId} executionId={''} variant={''} onAfterComplete={handleOnComplete} disabled>
            <GFormTip executionId={''} variant={''} onAfterComplete={handleOnComplete} />
          </WithFormProvider>
        </Container>
      </main>
    </GShell>
  </>)
}


