import React from 'react'
import { PublicationsTable } from '@/eveli-publications';
import { EveliApp } from '@/eveli-app';
import { EveliSetup } from '@/eveli-setup';


export const Route = createFileRoute({
  component: Component,
})

function Component() {
  return (
    <EveliApp main={PublicationsTable} secondary={() => <></>} toolbar={EveliSetup.Toolbar} tabs={() => <></>} />
  )
}
