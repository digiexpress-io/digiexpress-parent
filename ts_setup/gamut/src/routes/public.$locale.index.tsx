import React from 'react'
import { GRouterUnsecured } from '../g-router-unsecured';
import { useLocale } from '../api-locale';

export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useEffect(() => setLocale(locale), [locale])

  return (<GRouterUnsecured />)
}
