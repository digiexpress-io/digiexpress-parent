import React from 'react'

import { useLocale } from '@/api-locale';
import { UserProfile } from '@/eveli-user-profile';


export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();
  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<UserProfile />)
}
