import { createFileRoute } from '@tanstack/react-router'
import { Container } from '@mui/material'

//import { GShell, GFormTip, WithFormProvider } from '@dxs-ts/gamut'
export const Route = createFileRoute('/secured/$locale/assets/forms/$formId')({
  component: Component,
})
function Component() {
  const { formId } = Route.useParams();

  function handleOnComplete() {

  }
  
  return (<>
    {formId}----testing
    {/*<GShell drawerOpen={false}>
      <main role='main'>
        <Container>
          <WithFormProvider id={formId} executionId={''} variant={''} onAfterComplete={handleOnComplete}>
            <GFormTip executionId={''} variant={''} onAfterComplete={handleOnComplete} />
          </WithFormProvider>
        </Container>
      </main>
    </GShell>
    */
  }
  </>)
}


