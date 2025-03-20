import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale, EveliServices } from '@/burger'


export const Route = createFileRoute('/secured/$locale/assets/services/')({
  component: Component,
}) 

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();
  React.useLayoutEffect(() => setLocale(locale), [locale])
  return (<EveliServices />)
}
