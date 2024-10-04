import React from 'react'
import { Outlet, createFileRoute } from '@tanstack/react-router'
import { useLocale } from '../api-locale';


export const Route = createFileRoute('/public/$locale')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useEffect(() => setLocale(locale), [locale])
  return <Outlet />
}
