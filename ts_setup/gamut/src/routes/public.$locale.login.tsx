import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '../api-locale';
import { GErrorLogin } from '../g-error-login';


export const Route = createFileRoute('/public/$locale/login')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();
  React.useEffect(() => setLocale(locale), [locale])
  return <GErrorLogin />
}
