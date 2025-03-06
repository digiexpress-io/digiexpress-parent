import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/burger'
import { DialobAdminView } from '../frontdesk/views/forms/DialobAdminView';


export const Route = createFileRoute('/secured/$locale/assets/forms/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  
  const { setLocale } = useLocale();
  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<DialobAdminView />)
}
