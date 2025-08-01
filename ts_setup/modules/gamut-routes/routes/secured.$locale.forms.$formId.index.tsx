import { createFileRoute } from '@tanstack/react-router'
import { Container } from '@mui/material'


import { GShell, GFormTip } from '@dxs-ts/gamut-primitives'
import { WithFormProvider } from '@dxs-ts/gamut-api'

export const Route = createFileRoute('/secured/$locale/forms/$formId/')({
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
          <WithFormProvider id={formId} executionId={''} variant={''} onAfterComplete={handleOnComplete}>
            <GFormTip executionId={''} variant={''} onAfterComplete={handleOnComplete} />
          </WithFormProvider>
        </Container>
      </main>
    </GShell>
  </>)
}


