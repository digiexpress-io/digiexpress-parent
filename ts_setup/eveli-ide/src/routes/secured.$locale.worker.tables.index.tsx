import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { useLocale } from '@/api-locale';
import { EveliTable } from '@/eveli-table';

export const Route = createFileRoute('/secured/$locale/worker/tables/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<EveliTable />)
}
