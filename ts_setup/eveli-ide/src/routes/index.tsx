import { createFileRoute, Navigate } from '@tanstack/react-router'
import { useIam } from '@/api-iam';
import { EveliPermissionsNone } from '@/eveli-permissions-none';




export const Route = createFileRoute('/')({
  component: Component,
})

function Component() {
  const iam = useIam();
  if (iam.authType === 'ANON') {
    return <Navigate {...{
      from: '/',
      to: '/public/$locale/auth',
      params: { locale: 'en' }
    }} />
  } else if (iam.user.permissions.length === 0) {
    return (<EveliPermissionsNone />)
  } else {
    return <Navigate {...{
      from: '/',
      to: '/secured/$locale',
      params: { locale: 'en' }
    }} />
  }
}