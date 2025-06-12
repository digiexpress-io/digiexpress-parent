import React from 'react'
import { useLocale } from '@/api-locale';
import { PublicationsTable } from '@/eveli-publications';
import { EveliApp } from '@/eveli-app';
import { EveliSetup } from '@/eveli-setup';


export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { locale } = Route.useParams();
  const { setLocale } = useLocale();

  React.useLayoutEffect(() => setLocale(locale), [locale])

  return (
    <EveliApp main={PublicationsTable} secondary={() => <></>} toolbar={EveliSetup.Toolbar} tabs={() => <></>} />
  )
}
