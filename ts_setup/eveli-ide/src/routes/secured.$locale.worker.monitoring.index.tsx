import React from 'react'
import { EveliProcExecution } from '@/eveli-proc-execution';
import { useLocale } from '@/api-locale';



export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (<EveliProcExecution />)
}
