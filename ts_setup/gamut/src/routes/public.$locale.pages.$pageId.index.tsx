import React from 'react'
import { GRouterUnsecured } from '../g-router-unsecured'

export const Route = createFileRoute({
  component: Component,
})

function Component() {
  const { pageId } = Route.useParams()
  return React.useMemo(() => <ChooseComponent pageId={pageId} />, [pageId])
}

function ChooseComponent(props: { pageId: string }) {
  return <GRouterUnsecured pageId={props.pageId} />
}
