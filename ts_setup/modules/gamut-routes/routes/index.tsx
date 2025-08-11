import { Navigate, createFileRoute } from '@tanstack/react-router'
import { useIam, useLocale } from '@dxs-ts/gamut-api';


export const Route = createFileRoute('/')({
  component: Component,

})


function Component() {
  const iam = useIam();
  const { locale } = useLocale();
  if(iam.authType === 'ANON') {
    return <Navigate {...{
      from: '/',
      to: '/public/$locale',
      params: { locale }
    }}/>
  } else {
    return <Navigate {...{
      from: '/',
      to: '/secured/$locale/views/$viewId',
      params: { viewId: 'user-overview', locale }
    }}/>
  }
}