import { createFileRoute } from '@tanstack/react-router'
import React from 'react'
import { GRouterUnsecured } from '../g-router-unsecured';
import { useLocale } from '@dxs-ts/gamut-api';

export const Route = createFileRoute('/public/$locale/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useEffect(() => setLocale(locale), [locale])

  return (<GRouterUnsecured />)
}
