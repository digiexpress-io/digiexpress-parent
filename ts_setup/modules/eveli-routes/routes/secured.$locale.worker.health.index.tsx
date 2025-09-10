import { createFileRoute } from '@tanstack/react-router'
import { EveliProcExecution } from '../eveli-proc-execution';



export const Route = createFileRoute('/secured/$locale/worker/health/')({
  component: Component,
})

function Component() {
  return (<EveliProcExecution />)
}
