import { createFileRoute } from '@tanstack/react-router'
import React from 'react'
import { useLocale } from '@dxs-ts/gamut-api';
import { GErrorLogin } from '@dxs-ts/gamut-primitives';


export const Route = createFileRoute('/public/$locale/login')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();
  React.useEffect(() => setLocale(locale), [locale])
  return <GErrorLogin />
}
