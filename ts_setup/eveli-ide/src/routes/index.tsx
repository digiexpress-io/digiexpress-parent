import { createFileRoute, Navigate } from '@tanstack/react-router'
import { useIam } from '@/burger';


export const Route = createFileRoute('/')({
  component: Component,
})

function Component() {
  const iam = useIam();
  if(iam.authType === 'ANON') {
    return <Navigate {...{
      from: '/',
      to: '/public/$locale',
      params: { locale: 'en' }
    }}/>
  } else {
    return <Navigate {...{
      from: '/',
      to: '/secured/$locale',
      params: { locale: 'en' }
    }}/>
  }
}