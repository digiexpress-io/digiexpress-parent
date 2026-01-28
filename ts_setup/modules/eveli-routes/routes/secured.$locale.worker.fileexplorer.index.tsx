import { createFileRoute } from '@tanstack/react-router'
import { EveliTree } from '../../eveli-tree'

export const Route = createFileRoute('/secured/$locale/worker/fileexplorer/')({
  component: Component,
})

function Component() {
  return (<EveliTree />)
}