import React from 'react'

import { createFileRoute, useNavigate } from '@tanstack/react-router'
import { Container } from '@mui/material'

import { GShell } from '@dxs-ts/gamut-shell';
import { GFormUnavailable } from '@dxs-ts/gamut-primitives'
import { useIam, useLocale, WithFormProvider } from '@dxs-ts/gamut-api'
import { GFormTip } from '@dxs-ts/gamut-form'

export const Route = createFileRoute('/secured/$locale/forms/$formId/review')({
  component: Component,
})
function Component() {
  const { formId } = Route.useParams();
  const nav = useNavigate();
  const { authType } = useIam();
  const { locale } = useLocale();
  function handleOnComplete() {

  }
  
  const onCancel = React.useCallback(() => {
    if (authType === 'ANON') {
      nav({
        from: '/public/$locale/pages/$pageId/products/$productId',
        params: { locale },
        to: '/public/$locale'
      })
    } else {
      nav({
        from: '/secured/$locale/pages/$pageId/products/$productId',
        params: { viewId: 'user-overview' },
        to: '/secured/$locale/views/$viewId'
      })
    }
  }, [authType, locale]);

  return (<>
    <GShell drawerOpen={false}>
      <main role='main'>
        <Container>
          <WithFormProvider id={formId} executionId={''} variant={''} disabled onCancel={onCancel}
            onAfterComplete={handleOnComplete} formUnavailable={GFormUnavailable}>
            <GFormTip onCancel={onCancel} executionId={''} variant={''} onAfterComplete={handleOnComplete} formUnavailable={GFormUnavailable}/>
          </WithFormProvider>
        </Container>
      </main>
    </GShell>
  </>)
}


