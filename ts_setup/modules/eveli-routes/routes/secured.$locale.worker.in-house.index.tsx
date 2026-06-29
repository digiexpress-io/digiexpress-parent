import { createFileRoute } from '@tanstack/react-router'
import { EveliInHouse } from '../eveli-in-house'


export const Route = createFileRoute('/secured/$locale/worker/in-house/')({
  component: Component,
})

function Component() {

  
  return (<EveliInHouse />)
}




