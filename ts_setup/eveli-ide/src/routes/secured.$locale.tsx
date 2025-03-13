import { useIam } from '@/burger';
import { Navigate, Outlet, createFileRoute } from '@tanstack/react-router'


export const Route = createFileRoute('/secured/$locale')({
  component: Component,
})

function Component() {
  const iam = useIam();

  if(iam.authType === 'ANON') {
    return <Navigate {...{
      from: '/',
      to: '/public/$locale/auth',
      params: { locale: 'en' }
    }}/>
  }
  return <Outlet/>
}
