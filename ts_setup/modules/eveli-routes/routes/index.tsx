import { Navigate, createFileRoute } from '@tanstack/react-router'
import { useIam, useLocale } from '@dxs-ts/eveli-api';
import { EveliPermissionsNone } from '@dxs-ts/eveli-primitives';



export const Route = createFileRoute('/')({
  component: Component,
})

function Component() {
  const iam = useIam();
  const { locale } = useLocale();
  
  if (iam.authType === 'ANON') {
    return <Navigate {...{
      from: '/',
      to: '/public/$locale/auth',
      params: { locale }
    }} />
  } else if (iam.user.permissions.length === 0) {
    return (<EveliPermissionsNone />)
  } else {
    return <Navigate {...{
      from: '/',
      to: '/secured/$locale',
      params: { locale }
    }} />
  }
}