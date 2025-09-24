
import { createFileRoute } from '@tanstack/react-router'
import { PublicationsTable } from '@dxs-ts/eveli-routes';
import { EveliApp } from '../eveli-app';
import { EveliSetup } from '../eveli-setup';


export const Route = createFileRoute('/secured/$locale/publications/')({
  component: Component,
})

function Component() {
  return (
    <EveliApp main={PublicationsTable} secondary={() => <></>} toolbar={EveliSetup.Toolbar} tabs={() => <></>} />
  )
}
