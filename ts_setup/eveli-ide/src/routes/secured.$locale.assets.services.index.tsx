import React from 'react'
import { useLocale } from '@/api-locale';
import { EveliServices } from '@/eveli-services';


export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();
  React.useLayoutEffect(() => setLocale(locale), [locale])
  return (<EveliServices />)
}
