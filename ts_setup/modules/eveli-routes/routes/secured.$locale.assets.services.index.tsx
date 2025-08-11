import { createFileRoute } from '@tanstack/react-router'
import { EveliServices } from '../eveli-services';


export const Route = createFileRoute('/secured/$locale/assets/services/')({
  component: Component,
})

function Component() {
  return (<EveliServices />)
}
