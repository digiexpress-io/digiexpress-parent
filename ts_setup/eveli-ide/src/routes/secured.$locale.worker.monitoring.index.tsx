import React from 'react'
import { createFileRoute } from '@tanstack/react-router'
import { EveliProcExecution } from '@/eveli-proc-execution';
import { useLocale } from '@/api-locale';



export const Route = createFileRoute('/secured/$locale/worker/monitoring/')({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<EveliProcExecution />)
}
