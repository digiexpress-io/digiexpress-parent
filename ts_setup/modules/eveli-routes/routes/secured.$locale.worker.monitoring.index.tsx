import { createFileRoute } from '@tanstack/react-router'
import { EveliProcExecution } from '../eveli-proc-execution';



export const Route = createFileRoute('/secured/$locale/worker/monitoring/')({
  component: Component,
})

function Component() {
  return (<EveliProcExecution />)
}
