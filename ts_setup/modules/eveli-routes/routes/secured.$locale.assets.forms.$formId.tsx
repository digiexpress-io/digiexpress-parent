import { createFileRoute } from '@tanstack/react-router'
import { Container, createTheme, ThemeProvider } from '@mui/material'

import { WithFormProvider, DialobProvider, LocaleProvider } from '@dxs-ts/gamut-api'
import { GShell } from '@dxs-ts/gamut-shell'

import { useTenantConfig } from '@dxs-ts/eveli-api';
import { GFormTip } from '@dxs-ts/gamut-form';
import { useFetch } from '@dxs-ts/envir-fetch';

export const Route = createFileRoute('/secured/$locale/assets/forms/$formId')({
  component: Component,
})


function Component() {
  const { formId } = Route.useParams();
  const { gamutThemeOptions } = useTenantConfig();
  const siteTheme = createTheme(gamutThemeOptions);

  const { getDialobSession } = useFetch('worker/rest/api/assets/dialob/fill/$sessionId.GET', {});
  const { saveDialobSession } = useFetch('worker/rest/api/assets/dialob/fill/$sessionId.POST', {});

  function handleOnComplete() {

  }

  function handleCancel() {

  }
  
  return (
    <ThemeProvider theme={siteTheme}>
      <LocaleProvider>
        <DialobProvider
          fetchActionGet={getDialobSession}
          fetchActionPost={saveDialobSession}

          fetchReviewGet={{} as any}
          fetchAttachmentPost={() => {
            alert("M**L** preview mode feature does not support upload of attachments because we do not have 'PROCESS' entity!")
            return {} as any
          }}
        >
          <GShell drawerOpen={false}>
            <main role='main'>
              <Container>
                <WithFormProvider formUnavailable={FormUnavailable} onCancel={handleCancel} id={formId} executionId={''} variant={''} onAfterComplete={handleOnComplete}>
                  <GFormTip formUnavailable={FormUnavailable} onCancel={handleCancel} executionId={''} variant={''} onAfterComplete={handleOnComplete} />
                </WithFormProvider>
              </Container>
            </main>
          </GShell>
        </DialobProvider>
      </LocaleProvider>
    </ThemeProvider>)
}



function FormUnavailable() {
  return <>not available</>
}