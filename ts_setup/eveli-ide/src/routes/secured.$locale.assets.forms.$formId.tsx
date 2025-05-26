import { createFileRoute } from '@tanstack/react-router'
import { Components, Container, createTheme, ThemeOptions, ThemeProvider, Theme } from '@mui/material'
import { GShell, GFormTip, WithFormProvider, DialobProvider, LocaleApi, LocaleProvider } from '@dxs-ts/gamut'
import { useFetch } from '@dxs-ts/eveli-fetch';

export const Route = createFileRoute('/secured/$locale/assets/forms/$formId')({
  component: Component,
})


export const themeOptions: ThemeOptions = {};
const siteTheme = createTheme(themeOptions);


function Component() {
  const { formId } = Route.useParams();

  const { getDialobSession } = useFetch('worker/rest/api/assets/dialob/fill/$sessionId.GET', {});
  const { saveDialobSession } = useFetch('worker/rest/api/assets/dialob/fill/$sessionId.POST', {});

  function handleOnComplete() {

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
                <WithFormProvider id={formId} executionId={''} variant={''} onAfterComplete={handleOnComplete}>
                  <GFormTip executionId={''} variant={''} onAfterComplete={handleOnComplete} />
                </WithFormProvider>
              </Container>
            </main>
          </GShell>
        </DialobProvider>
      </LocaleProvider>
    </ThemeProvider>)
}





