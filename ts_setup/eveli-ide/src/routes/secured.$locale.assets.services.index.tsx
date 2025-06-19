import { EveliServices } from '@/eveli-services';


export const Route = createFileRoute({
  component: Component,
})

function Component() {
  return (<EveliServices />)
}
